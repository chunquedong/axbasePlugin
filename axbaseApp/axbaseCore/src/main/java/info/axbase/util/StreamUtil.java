/**
 * Axbase Project
 * Copyright (c) 2016 chunquedong
 * Licensed under the LGPL(http://www.gnu.org/licenses/lgpl.txt), Version 3
 */
package info.axbase.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.CRC32;

public class StreamUtil {

	public static int bufferSize = 1024*2;
	
	/**
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static void pipe(InputStream in, OutputStream out)
			throws IOException {
		try {
			byte[] bytes = new byte[bufferSize];
			int len = 0;
			while ((len = in.read(bytes)) != -1) {
				out.write(bytes, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] readAll(InputStream in) {
		byte[] buffer = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize);
			
			byte[] b = new byte[bufferSize];
			int n;
			while ((n = in.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			bos.close();
			buffer = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return buffer;
	}
	
	public static long crc32(InputStream in) {
		try {
			CRC32 crc32 = new CRC32();
			byte[] b = new byte[bufferSize];
			int n;
			while ((n = in.read(b)) != -1) {
				crc32.update(b, 0, n);
			}
			return crc32.getValue();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
}
