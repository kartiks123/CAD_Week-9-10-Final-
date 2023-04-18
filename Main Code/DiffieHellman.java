import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Scanner;

public class DiffieHellman 
{
    private final static SecureRandom random = new SecureRandom();
    private BigInteger int1;
    private BigInteger int2;

    private static class User
    {
        private BigInteger privatekey;
        private BigInteger publickey;
    }

    private DiffieHellman(int N) 
    {
        int1 = BigInteger.probablePrime(N, random);
        int2 = new BigInteger("3"); // Smallest prime number which is used as a shared key generator
    }

    private static void CreatePublicKey(User user, BigInteger int1, BigInteger int2)
    {
        int N;
        Scanner input = new Scanner(System.in);
        System.out.print("Enter key bit number : ");
        N = input.nextInt();
        BigInteger privatekey = new BigInteger(N, random);
     
        while(privatekey.compareTo(int1) == 1 || privatekey.compareTo(int1) == 0) 
        {
            System.out.print("Too big value! \nEnter key bit number again : ");
            N = input.nextInt();
            privatekey = new BigInteger(N, random);
        }
        
        BigInteger publickey = int2.modPow(privatekey,int1);
        user.privatekey = privatekey;
        user.publickey = publickey;
    }

    private static BigInteger CreatePrivateKey(BigInteger publickey, BigInteger privatekey, BigInteger int1)
    {
        return publickey.modPow(privatekey,int1);
    }

    private static void CrackKey(BigInteger int1, BigInteger int2, BigInteger pub_key1, BigInteger pub_key2)
    {
        BigInteger counter = new BigInteger("0");
        BigInteger key1 = new BigInteger("-1");
        BigInteger key2 = new BigInteger("-1");
        System.out.println("Public information that I know :-");
        System.out.println("int1 : " + int1);
        System.out.println("int2 : " + int2);
        System.out.println("Public Key 1 : " + pub_key1);
        System.out.println("Public Key 2 : " + pub_key2);
        
        long start = System.nanoTime();
        while(counter.compareTo(int1) < 0) // This loop is used to found the shared secret key
        {
            if(key1.compareTo(pub_key1) == 0)
            {
                break;
            }

            else 
            {
                counter = counter.add(BigInteger.ONE);
                key1 = int2.modPow(counter, int1);
            }
        }

        key1 = pub_key2.modPow(counter, int1);
        System.out.println("Key found : " + key1 );
        long elapsedTime = System.nanoTime() - start;
        System.out.println("First key cracked in : " + elapsedTime/1000000000.0 + " second");
    }

    public static void main(String[] args) 
    {
        int menu;
        DiffieHellman key = null;
        User user1 = null, user2 = null;
        Scanner input = new Scanner(System.in);
        System.out.println("~Welcome to Diffie-Hellman application~");
        while(true) 
        {
            System.out.println("These are the options available to you as our Menu :-");
            System.out.println("Type 1 to enter Diffie-Hellman initiation values");
            System.out.println("Type 2 for Diffie-Hellman User 1 keys creation");
            System.out.println("Type 3 for Diffie-Hellman User 2 keys creation");
            System.out.println("Type 4 for showing Diffie-Hellman User 1 common key with User 2");
            System.out.println("Type 5 for showing Diffie-Hellman User 2 common key with User 1");
            System.out.println("Type 6 to Exit");
            
            menu = input.nextInt();
            switch(menu) {
                case 0:
                    break;
                case 1:
                    System.out.println("Enter the number of bits");
                    int N = input.nextInt();
                    key = new DiffieHellman(N);
                    System.out.println("~Public Information~");
                    System.out.println("int1 : " + key.int1);
                    System.out.println("int2 : " + key.int2);
                    break;
                case 2:
                    user1 = new User();
                    System.out.println("Create keys for user1.");
                    CreatePublicKey(user1, key.int1, key.int2);
                    System.out.println("Your private key : " + user1.privatekey);
                    System.out.println("Your public key : " + user1.publickey);
                    break;
                case 3:
                    user2 = new User();
                    System.out.println("Create keys for user2.");
                    CreatePublicKey(user2, key.int1, key.int2);
                    System.out.println("Your private key : " + user2.privatekey);
                    System.out.println("Your public key : " + user2.publickey);
                    break;
                case 4:
                    BigInteger secretA = CreatePrivateKey(user2.publickey, user1.privatekey, key.int1);
                    System.out.println("SecretA is : " + secretA);
                    break;
                case 5:
                    BigInteger secretB = CreatePrivateKey(user1.publickey, user2.privatekey, key.int1);
                    System.out.println("SecretB is : " + secretB);
                    break;
                case 6:
                    CrackKey(key.int1, key.int2, user1.publickey, user2.publickey);
                    break;
                default:
                    System.out.println("Wrong Input.");
                    break;
            }
            if(menu==0) {
                break;
            }
        }
    }
}