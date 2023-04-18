import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class DiffieHellmanMapReduce extends Configured implements Tool {

    private static final BigInteger ONE = BigInteger.ONE;
    private static final SecureRandom random = new SecureRandom();
    private static final Random rand = new Random();

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new DiffieHellman(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        Job job = Job.getInstance(conf, "Diffie-Hellman Key Exchange");
        job.setJarByClass(DiffieHellman.class);
        job.setMapperClass(DiffieHellmanMapper.class);
        job.setReducerClass(DiffieHellmanReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path outputPath = new Path(args[1]);
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(outputPath)) {
            fs.delete(outputPath, true);
        }
        FileOutputFormat.setOutputPath(job, outputPath);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static class DiffieHellmanMapper extends Mapper<Object, Text, IntWritable, Text> {

        private final BigInteger p = generatePrime();
        private final BigInteger g = findPrimitiveRoot(p);
        private BigInteger a;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            a = generatePrivateKey(p);
        }

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                BigInteger B = new BigInteger(token);
                BigInteger secretKey = B.modPow(a, p);
                context.write(new IntWritable(rand.nextInt()), new Text(secretKey.toString()));
            }
        }

        private static BigInteger generatePrime() {
            BigInteger p, q;
            do {
                q = new BigInteger(256, random);
                p = q.multiply(ONE.shiftLeft(1)).add(ONE);
            } while (!p.isProbablePrime(50));
            return p;
        }

        private static BigInteger findPrimitiveRoot(BigInteger p) {
            BigInteger g;
            do {
                g = new BigInteger(p.bitLength(), random);
            } while (!g.modPow(p.subtract(ONE).divide(BigInteger.valueOf(2)), p).equals(p.subtract(ONE)));
            return g;
        }

        private static BigInteger generatePrivateKey(BigInteger p) {
            return new BigInteger(p.bitLength() - 1, random);
        }
    }

    public static class DiffieHellmanReducer extends Reducer<IntWritable, Text, IntWritable, Text> {

        private BigInteger b;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
        b = DiffieHellmanMapper.generatePrivateKey(((DiffieHellmanMapper)context.getMapper()).p);
    }

    @Override
    protected void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        BigInteger sharedSecret = null;
        for (Text value : values) {
            BigInteger A = new BigInteger(value.toString());
            sharedSecret = A.modPow(b, ((DiffieHellmanMapper)context.getMapper()).p);
        }
        context.write(key, new Text(sharedSecret.toString()));
    }
}

