/*

Copyright 2021 aholinch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package odutils.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.ImageIcon;

/**
 * A set of utilities for manipulating files.
 */
public class FileUtil
{
	public static void sortByDate(List<File> files, boolean descending)
	{
		Collections.sort(files, new FileDateSorter(descending));
	}
	
	public static String checkSum(String file)
	{
	     FileInputStream fis =  null;
	     String str = null;
	     try
	     {
	    	 fis = new FileInputStream(file);

		     byte ba[] = new byte[1024];
		     MessageDigest md5 = MessageDigest.getInstance("MD5");
		     int numRead = 0;
		     
		     numRead = fis.read(ba);
		     while(numRead > 0)
		     {
		    	 md5.update(ba, 0, numRead);
		    	 numRead = fis.read(ba);
			 }
		     
		     ba = md5.digest();
		     BigInteger bi = new BigInteger(ba);
		     str = bi.toString(16);
	     }
	     catch(Exception ex)
	     {
	    	 ex.printStackTrace();
	     }
	     finally
	     {
	    	 if(fis != null)try{fis.close();}catch(Exception ex){}
	     }

	     return str;
	}
	
	public static String latestInDir(String sdir, boolean dirsOnly)
	{
		try
		{
			File dir = new File(sdir);
			File files[] = dir.listFiles();
			long oldest = System.currentTimeMillis()+10000l;
			long t = 0;
			File oldf = null;
			
			File f = null;
			int nf = files.length;
			if(nf == 0) return null;
			
			for(int i=0; i<nf; i++)
			{
				f = files[i];
				if(dirsOnly && !f.isDirectory())continue;
				t = f.lastModified();
				if(t < oldest)
				{
					oldest = t;
					oldf = f;
				}
			}
			if(oldf == null) return null;
			
			return oldf.getAbsolutePath();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Copy the contents of the first file to the second file.
	 * 
	 * @param srcFile
	 * @param dstFile
	 */
	public static void copyFile(String srcFile, String dstFile)
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		byte ba[] = new byte[10240];
		int numRead = 0;
		try
		{
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(dstFile);
			
			numRead = fis.read(ba);
			while(numRead > 0)
			{
				fos.write(ba,0,numRead);
				numRead = fis.read(ba);
			}
			
			fos.flush();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			close(fis);
			close(fos);
		}
	}
	
    /**
     * Return an input stream to the file.  Check the file system first
     * next look on the classpath.
     */
    @SuppressWarnings("resource")
	public static InputStream getInputStream(String filename)
    {
        InputStream is = null;

        try
        {
            FileInputStream fis = new FileInputStream(filename);

            is = fis;
        }
        catch(Exception ex)
        {
            // that's OK it might be in a jar file or elsewhere on the classpath.

            //ex.printStackTrace();
        }

        // check the classpath
        if(is == null)
        {
            try
            {
                is = odutils.util.FileUtil.class.getResourceAsStream(filename);
            }
            catch(Exception ex)
            {
            	//ex.printStackTrace();
            }
        }

        if(is == null)
        {
            try
            {
                is = odutils.util.FileUtil.class.getResourceAsStream("/"+filename);
            }
            catch(Exception ex)
            {
            	//ex.printStackTrace();
            }
        }

        if(is == null)
        {
            try
            {
                URL url = odutils.util.FileUtil.class.getResource(filename);
                is = url.openStream();
            }
            catch(Exception ex)
            {
            	//ex.printStackTrace();
            }
        }

        if(is == null)
        {
            try
            {
                URL url = odutils.util.FileUtil.class.getResource("/"+filename);
                is = url.openStream();
            }
            catch(Exception ex)
            {
            	//ex.printStackTrace();
            }
        }
        
        if(is == null)
        {
	        try
	        {
	            FileInputStream fis = new FileInputStream("./"+filename);
	
	            is = fis;
	        }
	        catch(Exception ex)
	        {
	            // that's OK it might be in a jar file or elsewhere on the classpath.
	
	            //ex.printStackTrace();
	        }
        }

        return is;
    }

    /**
     * Get a reader to the specified resource.
     */
    public static Reader getReader(String filename)
    {
        InputStreamReader isr = null;

        InputStream is = getInputStream(filename);

        if(is != null)
        {
            try
            {
                isr = new InputStreamReader(is);
            }
            catch(Exception ex)
            {
            	ex.printStackTrace();
            }
        }

        return isr;
    }
    
    /**
     * Close the output stream and trap any exceptions.
     */
    public static void close(OutputStream os)
    {
    	if(os != null)
        {
    		try
	        {
                os.close();
	        }
	        catch(Exception ex)
	        {
	        }
    	}
    }
    
    public static void close(Writer fw)
    {
    	if(fw != null)
    	{
    		try{fw.close();}catch(Exception ex){};
    	}
    }
    
    /**
     * Close the input stream and trap any exceptions.
     */
    public static void close(InputStream is)
    {
    	if(is != null)
        {
    		try
	        {
                is.close();
	        }
	        catch(Exception ex)
	        {
	        }
    	}
    }
    
    /**
     * Close the reader.
     * 
     * @param r
     */
    public static void close(Reader r)
    {
    	if(r != null)
    	{
    		try
    		{
    			r.close();
    		}
    		catch(Exception ex)
    		{
    			
    		}
    	}
    }
    
    /**
     * Dump the input stream to the specified file.
     * 
     * @param is
     * @param filename
     */
    public static void dumpStreamToFile(InputStream is, String filename)
    {
    	FileOutputStream fos = null;
    	
    	try
    	{
    		fos = new FileOutputStream(filename);
    		dumpStreamToStream(is,fos);
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		close(fos);
    	}
    }
    
    /**
     * Dump the input stream to the output stream.
     * 
     * @param is
     * @param os
     */
    public static void dumpStreamToStream(InputStream is, OutputStream os)
    {
    	byte ba[] = new byte[2048];
    	int numRead = 0;
    	try
    	{
    		numRead = is.read(ba);
    		while(numRead > 0)
    		{
    			os.write(ba,0,numRead);
    			numRead = is.read(ba);	
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(os != null)try{os.flush();}catch(Exception ex){};
    	}
    }
    
    /**
     * Read the bytes from the file.
     * 
     * @param filename
     * @return
     */
    public static byte[] getBytesFromFile(String filename)
    {
    	byte ba[] = null;
    	
    	InputStream is = getInputStream(filename);
    	try
    	{
    		ba = getBytesFromStream(is);
    	}
    	finally
    	{
    		close(is);
    	}
    	
    	return ba;
    }
    
    /**
     * Convert the inputstream into a byte array.
     * 
     * @param is
     * @return
     */
    public static byte[] getBytesFromStream(InputStream is)
    {
    	return getBytesFromStream(is,2048);
    }
    
    /**
     * Convert the inputstream into a byte array.
     * 
     * @param is
     * @return
     */
    public static byte[] getBytesFromStream(InputStream is, int bufSize)
    {
    	byte ba[] = new byte[bufSize];
    	
    	try
    	{
    		List bytes = new ArrayList();
    		List lengths = new ArrayList();
    		
    		int tot = 0;
	    	int numRead = is.read(ba);
	    	while(numRead > 0)
	    	{
	    		tot+=numRead;
	    		bytes.add(ba);
	    		lengths.add(new Integer(numRead));
    			ba = new byte[bufSize];
	    		numRead = is.read(ba);
	    	}
	    	
	    	ba = new byte[tot];
	    	byte baTmp[] = null;
	    	
	    	int offset = 0;
	    	int size = bytes.size();
	    	for(int i=0; i<size; i++)
	    	{
	    		baTmp = (byte[])bytes.get(i);
	    		numRead = ((Integer)lengths.get(i)).intValue();
	    		/*
	    		numRead = baTmp.length;
	    		if(offset + numRead > tot)
	    		{
	    			numRead = tot-offset;
	    		}
	    		*/
	    		System.arraycopy(baTmp,0,ba,offset,numRead);
	    		
	    		offset+=numRead;
	    	}
	    	bytes.clear();
	    	lengths.clear();
	    	bytes = null;
	    	lengths = null;
    	}
    	catch(Exception ex)
    	{
    		ba = null;
    		ex.printStackTrace();
    	}
    	
    	return ba;
    }
            
    /**
     * Read the contents of the file into a string.
     * 
     * @param filename
     * @return
     */
    public static String getStringFromFile(String filename)
    {
        String str = null;
       
        str = getStringFromFile(filename,"UTF-8");
        
        return str;
    }

    public static String getStringFromFile(String filename,String encoding)
    {
    	String lc = filename.toLowerCase();
    	if(lc.endsWith(".zip") || lc.endsWith(".gz"))
    	{
    		return getStringFromZipFile(filename,encoding);
    	}
    	
        String str = null;
       
        byte ba[] = getBytesFromFile(filename);
        
        if(ba != null && ba.length > 0)
        {
        	try
        	{
        		str = new String(ba,encoding);
        	}
        	catch(Exception ex)
        	{
        		ex.printStackTrace();
        	}
        }
        
        return str;
    }
    
    /**
     * Assumes only one entry in zip file.  Streams gz.
     * 
     * @param file
     * @param encoding
     * @return
     */
	public static String getStringFromZipFile(String file)
    {
		return getStringFromZipFile(file,"UTF-8");
    }
	
	/**
     * Assumes only one entry in zip file.  Streams gz.
     * 
     * @param file
     * @param encoding
     * @return
     */
	@SuppressWarnings("unchecked")
	public static String getStringFromZipFile(String file, String encoding)
    {
    	String out = null;
    	
    	InputStream is = null;
    	FileInputStream fis = null;
    	ZipFile zf = null;
    	
    	String lc = file.toLowerCase();
    	if(lc.endsWith(".zip"))
    	{
    		try
    		{
    			zf = new ZipFile(file);
    			Enumeration<ZipEntry> ezf = (Enumeration<ZipEntry>) zf.entries();
    			if(ezf.hasMoreElements())
    			{
    				ZipEntry ze = ezf.nextElement();
    				is = zf.getInputStream(ze);
    			}
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    	else if(lc.endsWith(".gz"))
    	{
    		try
    		{
    			fis = new FileInputStream(file);
    			is = new GZIPInputStream(fis);
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    	
    	if(is != null)
    	{
    		try
    		{
    			byte ba[] = getBytesFromStream(is);
    			out = new String(ba,encoding);
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    	
    	close(fis);
    	close(is);
    	if(zf != null)try{zf.close();}catch(Exception ex){};
    	
    	return out;
    }

	
	public static String getStringFromGZ(String file, String encoding)
    {
    	String out = null;
    	
    	InputStream is = null;
    	FileInputStream fis = null;
    	ZipFile zf = null;
    	
    	
		try
		{
			fis = new FileInputStream(file);
			is = new GZIPInputStream(fis);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	
    	if(is != null)
    	{
    		try
    		{
    			byte ba[] = getBytesFromStream(is);
    			out = new String(ba,encoding);
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    	}
    	
    	close(fis);
    	close(is);
    	if(zf != null)try{zf.close();}catch(Exception ex){};
    	
    	return out;
    }

    /**
     * Write the given lines to a file, uses \n for new lines
     * @param filename
     * @param lines
     */
    public static void dumpLinesToFile(String filename, List lines)
    {
    	FileWriter fw = null;
    	
    	try
    	{
    		fw = new FileWriter(filename);
    		int size = lines.size();
    		for(int i=0; i<size; i++)
    		{
    			fw.write(lines.get(i).toString());
    			fw.write("\n");
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(fw != null)try{fw.flush();}catch(Exception ex){};
    		close(fw);
    	}
    }
    
    /**
     * Return the lines of a file.
     * 
     * @param filename
     * @return
     */
    public static List getLinesFromFile(String filename)
    {
    	List lines = new ArrayList();
    	//System.out.println(filename);
    	Reader r = getReader(filename);
    	
    	try
    	{
    		LineNumberReader lnr = new LineNumberReader(r);
    		
    		String line = null;
    		
    		line = lnr.readLine();
    		while(line != null)
    		{
    			lines.add(line);
    			line = lnr.readLine();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    	    close(r);	
    	}
    	
    	return lines;
    }
 
    /**
     * Reads the lines from the file starting where specified for the
     * number of lines requested.
     * 
     * @param filename
     * @param start
     * @param numLines
     * @return
     */
    public static List getLinesFromFile(String filename, int start, int numLines)
    {
    	List lines = new ArrayList();
    	//System.out.println(filename);
    	Reader r = getReader(filename);
    	
    	try
    	{
    		LineNumberReader lnr = new LineNumberReader(r);
    		
    		String line = null;
    		
    		line = lnr.readLine();
    		int ln = 0;
    		while(line != null && ln < start)
    		{
    			ln++;
    			line = lnr.readLine();
    		}
    		
    		ln = 0;
    		while(line != null && ln < numLines)
    		{
    			ln++;
    			lines.add(line);
    			line = lnr.readLine();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    	    close(r);	
    	}
    	
    	return lines;
    }

    /**
     * Returns a gray scale image from the stream.
     * 
     * @param is
     * @return
     */
    public static BufferedImage getImageFromStream(InputStream is)
    {
    	return getImageFromStream(is,BufferedImage.TYPE_BYTE_GRAY);
    }
    
    /**
     * Returns an image of the specified pixel type.
     * 
     * @param is
     * @param type
     * @return
     */
    public static BufferedImage getImageFromStream(InputStream is, int type)
    {
    	BufferedImage bi = null;
		try
		{
			ImageIcon icon = new ImageIcon(FileUtil.getBytesFromStream(is));
			int ny = icon.getIconHeight();
			int nx = icon.getIconWidth();

			bi = new BufferedImage(nx, ny, type);
			Graphics2D g = bi.createGraphics();
			g.drawImage(icon.getImage(), 0, 0, null);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			FileUtil.close(is);
		}
		
		return bi;
    }

    /**
     * Removes any directory information from string and returns filename.
     * 
     * @param filename
     * @return
     */
	public static String getFilename(String filename)
	{
		if(filename == null)
		{
			return null;
		}
		
		filename = filename.replace('\\','/');
		int ind = filename.lastIndexOf('/');
		if(ind == -1)
		{
			return filename;
		}
		
		if(ind == (filename.length()-1))
		{
			return "";
		}
		
		return filename.substring(ind+1);
	}
	
    /**
     * Gets the directory portion of the filename.
     * 
     * @param filename
     * @return
     */
	public static String getDirectoryName(String filename)
	{
		if(filename == null)
		{
			return null;
		}
		
		filename = filename.replace('\\','/');
		int ind = filename.lastIndexOf('/');
		if(ind == -1)
		{
			return "";
		}
		
		if(ind == (filename.length()-1))
		{
			return filename;
		}
		
		return filename.substring(0,ind+1);
	}

	/**
	 * Attempts to get the data from the url (HTTP GET).
	 * It will use an internal cache while it's reading
	 * and then put the whole thing in a single byte array.
	 * This causes the utility to need twice the amount of 
	 * memory to build the output.
	 * 
	 * @param urlStr
	 * @return
	 */
	public static byte[] downloadBytesFromURL(String urlStr)
	{
		byte baOut[] = null;

		HttpURLConnection conn = null;
		InputStream is = null;
		
		try
		{
			URL url = new URL(urlStr);
			conn = (HttpURLConnection)url.openConnection();
			is = conn.getInputStream();

			baOut = getBytesFromStream(is);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			FileUtil.close(is);
			if(conn != null)try{conn.disconnect();}catch(Exception ex){};
		}
		
		return baOut;
	}

	/**
	 * Add the contents of the file to the existing zip.
	 * 
	 * @param newZipFile
	 * @param oldZipFile
	 */
	public static void addFileToZip(String newZipFile, String oldZipFile, String newFile, String excludeExtension)
	{
		ZipFile oldZip = null;
		ZipOutputStream zos = null;
		InputStream zis = null;
		ZipEntry ze = null;
		String filename = null;
		
		try
		{
			zos = new ZipOutputStream(new FileOutputStream(newZipFile));
			oldZip = new ZipFile(oldZipFile);
			Enumeration entries = oldZip.entries();
			while(entries.hasMoreElements())
			{
				ze = (ZipEntry)entries.nextElement();
				filename = ze.getName();
				if(filename != null && !filename.endsWith(excludeExtension))
				{
					zis = oldZip.getInputStream(ze);
					zos.putNextEntry(ze);
					dumpStreamToStream(zis,zos);
					zos.closeEntry();
				}
			}
			
			File f = new File(newFile);
			zis = new FileInputStream(newFile);
			ze = new ZipEntry(f.getName());
			ze.setTime(f.lastModified());
			ze.setSize(f.length());
			
			zos.putNextEntry(ze);
			dumpStreamToStream(zis,zos);
			zos.closeEntry();
			
			zos.flush();
			zos.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try{oldZip.close();}catch(Exception ex){};
			close(zos);
			close(zis);
		}
	}
	
	public static String getStringFromBytes(byte ba[])
	{
		return getStringFromBytes(ba,"UTF-8");
	}
	public static String getStringFromBytes(byte ba[],String encoding)
	{
		String out = null;
		ByteArrayInputStream bais = null;
		try
		{
			bais = new ByteArrayInputStream(ba);
			out = getStringFromStream(bais,encoding);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			close(bais);
		}

		return out;
	}
	
	public static String getStringFromStream(InputStream is)
	{
		return getStringFromStream(is,"UTF-8");
	}
	public static String getStringFromStream(InputStream is,String encoding)
	{
		StringBuilder sb = new StringBuilder(10000);
		InputStreamReader isr = null;
		char cs[] = new char[2048];
		try
		{
			isr = new InputStreamReader(is,encoding);
			int numRead = 1;
			while(numRead > 0)
			{
				numRead = isr.read(cs);
				if(numRead > 0)
				{
					sb.append(cs,0,numRead);
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			close(isr);
		}
		return sb.toString();
	}
	
	protected static class FileDateSorter implements Comparator<File>
	{
		protected boolean descending = false;
		
		public FileDateSorter(boolean flag)
		{
			descending = flag;
		}

		@Override
		public int compare(File o1, File o2)
		{
			Long t1 = o1.lastModified();
			Long t2 = o2.lastModified();
			
			
			int comp = t1.compareTo(t2);
			if(descending)
			{
				comp*=-1;
			}
			
			return comp;
		}
		
	}

	public static String firstLine(String file) 
	{
		return firstLine(file,"UTF-8");
	}
	
	@SuppressWarnings("unchecked")
	public static String firstLine(String file, String enc)
	{
		FileInputStream fis = null;
		InputStream is = null;
		ZipFile zf = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		String line = null;
		try
		{
			String name = file.toLowerCase();
			if(name.endsWith(".zip"))
			{
    			zf = new ZipFile(file);
    			Enumeration<ZipEntry> ezf = (Enumeration<ZipEntry>) zf.entries();
    			if(ezf.hasMoreElements())
    			{
    				ZipEntry ze = ezf.nextElement();
    				is = zf.getInputStream(ze);
    			}	
			}
			else if(name.endsWith(".gz"))
			{
				fis = new FileInputStream(file);
				is = new GZIPInputStream(fis);
			}
			else
			{
				fis = new FileInputStream(file);
				is = fis;
			}
			
			isr = new InputStreamReader(is,enc);
			br = new BufferedReader(isr);
			line = br.readLine();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			close(is);
			close(fis);
			close(isr);
			close(br);
			if(zf != null)try{zf.close();}catch(Exception ex){}
		}
		
		return line;
	}
}
