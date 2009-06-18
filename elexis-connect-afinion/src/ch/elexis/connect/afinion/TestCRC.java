package ch.elexis.connect.afinion;

import java.util.zip.CRC32;

public class TestCRC {
	
	private static void test(byte[] bytes) {

        /**************************************************************************
         *  Using direct calculation
         **************************************************************************/

         int crc  = 0xFFFFFFFF;       // initial contents of LFBSR
         int poly = 0xEDB88320;   // reverse polynomial

         for (byte b : bytes) {
             int temp = (crc ^ b) & 0xff;

             // read 8 bits one at a time
             for (int i = 0; i < 8; i++) {
                 if ((temp & 1) == 1) temp = (temp >>> 1) ^ poly;
                 else                 temp = (temp >>> 1);
             }
             crc = (crc >>> 8) ^ temp;
         }

         // flip bits
         crc = crc ^ 0xffffffff;

         System.out.println("CRC32 (via direct calculation) = " + Integer.toHexString(crc));



        /**************************************************************************
         *  Using Java's java.util.zip.CRC32 library
         **************************************************************************/
         java.util.zip.CRC32 x = new java.util.zip.CRC32();
         x.update(bytes);
         System.out.println("CRC32 (via Java's library)     = " + Long.toHexString(x.getValue()));
    }
   
    public static void main(String[] args) {
        byte[] input = new byte[] { 0x10, 0x02,
                0x30, 0x30, 0x30, 0x31, 0x30,
                0x30, 0x30, 0x30, 0x3A, 0x64,
                0x65, 0x62, 0x75, 0x67, 0x6D,
                0x73, 0x67, 0x40, 0x53, 0x65,
                0x6E, 0x73, 0x6F, 0x72, 0x44,
                0x69, 0x66, 0x66, 0x43, 0x68,
                0x65, 0x63, 0x6B, 0x20, 0x73,
                0x74, 0x61, 0x72, 0x74, 0x65, 0x64,
                0x10, 0x17 };
       
        // Expected checksum = 0x45, 0x38, 0x46, 0x44 (1161315908)

        CRC32 crc32 = new CRC32();
        crc32.reset();
        crc32.update(input);
        System.out.println("CRC32 = " + Long.toHexString(crc32.getValue()));
        
        test(input);
    }

}
