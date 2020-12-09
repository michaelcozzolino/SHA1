package com.michaelcozzolino.sha1;

import com.google.common.base.Splitter;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.*;

public class Sha1 {
    private final String h0;
    private final String h1;
    private final String h2;
    private final String h3;
    private final String h4;

    public Sha1(){
        this.h0 = "67452301";
        this.h1 = "efcdab89";
        this.h2 = "98badcfe";
        this.h3 = "10325476";
        this.h4 = "c3d2e1f0";
    }

    public Map<String,String> compute(String stringToBeHashed){
        List<String> messageBlocks = this.preprocess(stringToBeHashed);
        int blocksNumber = messageBlocks.size();
        String h0 = this.hexadecimalToBinary(this.h0,32);
        String h1 = this.hexadecimalToBinary(this.h1,32);
        String h2 = this.hexadecimalToBinary(this.h2,32);
        String h3 = this.hexadecimalToBinary(this.h3,32);
        String h4 = this.hexadecimalToBinary(this.h4,32);

        for(int i = 0; i < blocksNumber; i++){
            String a = this.prependZeros(h0,32);
            String b = this.prependZeros(h1,32);
            String c = this.prependZeros(h2,32);
            String d = this.prependZeros(h3,32);
            String e = this.prependZeros(h4,32);
            List<String> WtStrings = new ArrayList<>();
            for(int t = 0; t <= 79; t++){

                String Kt = this.computeBinaryKt(t);
                String Wt = "";

                if(inRange(t,0,15)){
                   Wt = messageBlocks.get(i).substring(32*t,32*t + 32);
                }
                else if(inRange(t,16,79)){
                    BigInteger Wt_3Int = new BigInteger(WtStrings.get(t - 3),2);
                    BigInteger Wt_8Int = new BigInteger(WtStrings.get(t - 8),2);
                    BigInteger Wt_14Int = new BigInteger(WtStrings.get(t - 14),2);
                    BigInteger Wt_16Int = new BigInteger(WtStrings.get(t - 16),2);
                    String xor = this.prependZeros(Wt_3Int.xor(Wt_8Int).xor(Wt_14Int).xor(Wt_16Int).toString(2),32);
                    Wt = this.ROTL(xor,1);
                }
                WtStrings.add(Wt);

                List<String> tData = new ArrayList<>(Arrays.asList(this.ROTL(a,5),this.computeFt(t,b,c,d),e,Kt,Wt));
                String T = this.binarySum(tData,32);

                e = d;
                d = c;
                c = this.ROTL(this.prependZeros(b,32),30);
                b = a;
                a = T;

            }
            h0 = this.binarySum(new ArrayList<>(Arrays.asList(a,h0)),32);
            h1 = this.binarySum(new ArrayList<>(Arrays.asList(b,h1)),32);
            h2 = this.binarySum(new ArrayList<>(Arrays.asList(c,h2)),32);
            h3 = this.binarySum(new ArrayList<>(Arrays.asList(d,h3)),32);
            h4 = this.binarySum(new ArrayList<>(Arrays.asList(e,h4)),32);

        }
        Map<String,String> hashedString = new LinkedHashMap<>();
        String binaryHashedString = h0 + h1 + h2 + h3 + h4;
        String hexadecimalHashedString = this.binaryToHexadecimal(binaryHashedString);
        hashedString.put("BINARY",binaryHashedString);
        hashedString.put("HEXADECIMAL",hexadecimalHashedString.toUpperCase());
        return hashedString;
    }

    private List<String> preprocess(String stringToBeHashed){

        List<String> chunkedStringToBeHashed = this.StringChunk(stringToBeHashed,512);
        List<String> binaryChunkedStringToBeHashed = new ArrayList<>();
        for (String chunk : chunkedStringToBeHashed ) {
            byte decimalChunk[] = chunk.getBytes(Charset.forName("UTF-8"));
            int chunkLength = decimalChunk.length;
            StringBuilder binaryChunk = new StringBuilder();
            for(int i = 0; i < chunkLength; i++) {
               binaryChunk.append(this.decimalToBinary(decimalChunk[i], 8));
            }

            if(binaryChunk.length() != 512){
                //we are in the last chunk
                binaryChunk.append("1"); //appending 1 to the end of message
                // l + 1 + k = 448 mod 512
                int chunkToBeHashedLength = chunk.length();
                int l = 8*chunkToBeHashedLength; //length of chunk in bits ( one character is on 8 bits)
                int k = Math.abs( 447 - l); //numbers of zeros to be added

                binaryChunk.append(this.decimalToBinary(0,k));
                binaryChunk.append(this.decimalToBinary(8*stringToBeHashed.length(),64));

            }
            binaryChunkedStringToBeHashed.add(String.valueOf(binaryChunk));
        }
        return binaryChunkedStringToBeHashed;

    }
    private boolean inRange(int value,int min,int max){
        return value >= min && value <= max;
    }

    private String prependZeros(String string, int numBits){
        int stringLength = string.length();
        if(stringLength < numBits){
            int zerosToPrepend = numBits - stringLength;
            StringBuilder temp = new StringBuilder();
            for( int i = 0; i < zerosToPrepend; i++){
                temp.append("0");
            }
            return temp.append(string).toString();
        }
        else if(stringLength > numBits){
            System.err.println("Number of bits in the string must be lower or equal to the number of specified bits!");
            System.exit(1);
        }
        return string;
    }

    private String binarySum(List<String> binaryValues,int numBits){
        BigInteger sum = new BigInteger(binaryValues.get(0),2);
        int binaryValuesLength = binaryValues.size();
        for(int i = 1; i < binaryValuesLength; i++){
            sum = sum.add(new BigInteger(binaryValues.get(i),2));
        }

        String stringSum = sum.toString(2);
        int stringSumLength = stringSum.length();
        if(stringSumLength > numBits)
            stringSum = stringSum.substring(stringSumLength - numBits); //removing potential carries
        else
            stringSum = this.prependZeros(stringSum,numBits);

        return stringSum;

    }

    private String ROTL(String value, int numBitsToRotate){
        if(value.length() < numBitsToRotate){
            System.err.println("the number of bits of the string to be used must be greater than the number of bits used to rotate it!");
            System.exit(1);
        }

        String begin = value.substring(numBitsToRotate);
        String end = value.substring(0,numBitsToRotate);
        return begin + end;
    }

   private String choose(String x, String y, String z){
        BigInteger xInt = new BigInteger(x,2);
        BigInteger yInt = new BigInteger(y,2);
        BigInteger zInt = new BigInteger(z,2);
        return ((xInt.and(yInt)).xor(xInt.not().and(zInt))).toString(2);

   }

   private String majority(String x, String y, String z){
       BigInteger xInt = new BigInteger(x,2);
       BigInteger yInt = new BigInteger(y,2);
       BigInteger zInt = new BigInteger(z,2);
       return ((xInt.and(yInt)).xor(xInt.and(zInt))).xor(yInt.and(zInt)).toString(2);
   }

    private String parity(String x, String y, String z){
        BigInteger xInt = new BigInteger(x,2);
        BigInteger yInt = new BigInteger(y,2);
        BigInteger zInt = new BigInteger(z,2);
        return xInt.xor(yInt).xor(zInt).toString(2);
    }

    private String computeFt(int t, String x, String y, String z){
        if (this.inRange(t,0,19))
            return this.choose(x,y,z);
        else if (this.inRange(t,20,39) || this.inRange(t,60,79))
            return this.parity(x,y,z);
        else if (this.inRange(t,40,59))
            return this.majority(x,y,z);
        else {
            System.err.println("t is out of range");
            System.exit(1);
            return null;
        }
    }

    private String computeBinaryKt(int t){
        if (this.inRange(t,0,19))
            return this.hexadecimalToBinary("5a827999",32);
        else if (this.inRange(t,20,39))
            return this.hexadecimalToBinary("6ed9eba1",32);
        else if (this.inRange(t,40,59))
            return this.hexadecimalToBinary("8f1bbcdc",32);
        else if (this.inRange(t,60,79))
            return this.hexadecimalToBinary("ca62c1d6",32);
        else {
            System.err.println("t is out of range");
            System.exit(1);
            return null;
        }
    }

    private List<String> StringChunk(String string,int maxChunkLengthBits){
        List<String> chunks = new ArrayList<>();
        for(final String chunk : Splitter.fixedLength(maxChunkLengthBits/8).split(string)){
            chunks.add(chunk);
        }
        return chunks;
    }

    private String hexadecimalToBinary(String hexadecimal,int numBits){
        String binary = new BigInteger(hexadecimal,16).toString(2);
        return this.prependZeros(binary,numBits);
    }

    private String binaryToHexadecimal(String binary){
        String hexadecimal = new BigInteger(binary,2).toString(16);
        return hexadecimal;
    }

    private String decimalToBinary(int value, int numBits){
        String binaryString = Integer.toBinaryString(value);
        return this.prependZeros(binaryString,numBits);
    }
}
