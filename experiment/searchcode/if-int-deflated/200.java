package org.solrsystem.files;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.solrsystem.DeflatedStorage;
import org.solrsystem.IdUtils;
import org.solrsystem.StorageService;

public class FileSystemStorage implements StorageService, DeflatedStorage {

	private int bufferSize = 32 * 1024;

	private int deflationTarget = 8 * 1024;

	private final File baseDirectory;

	private final File dataDirectory;

	private final MessageDigest _sha256;

	public FileSystemStorage(File baseDir) throws NoSuchAlgorithmException {
		baseDirectory = baseDir;
		_sha256 = MessageDigest.getInstance("SHA-256");
		dataDirectory = new File(baseDir, "data");
	}

	public void create() throws IOException {
		if (baseDirectory.exists())
			throw new IOException(format("the directory %s already exists",
					baseDirectory.getAbsolutePath()));
		dataDirectory.mkdirs();
		// TODO: properties file
	}

	public boolean hasData(byte[] id) throws IOException {
		if (id == null)
			return false;
		File storageFile = getStorageFile(IdUtils.toString(id));
		if (storageFile.exists())
			return true;
		// compressed files have a ".size" file
		storageFile = new File(storageFile.getParentFile(), storageFile
				.getName()
				+ ".size");
		return storageFile.exists();
	}

	public InputStream readData(byte[] id) throws IOException {
		File file = getStorageFile(IdUtils.toString(id));
		if (file.exists())
			return new FileInputStream(file);
		// check the deflated file
		file = new File(file.getParentFile(), file.getName() + ".deflated");
		if (file.exists())
			return new InflaterInputStream(new FileInputStream(file));
		return null;

	}

	public byte[] storeData(InputStream data) throws IOException {
		// clone digest for thread safety
		try {
			return storeData(data, (MessageDigest) _sha256.clone());
		} catch (CloneNotSupportedException e) {
			// use original digest
			synchronized (_sha256) {
				return storeData(data, _sha256);
			}
		}
	}

	private File getStorageFile(String id) {
		File directory = dataDirectory;
		// TODO: directory hash
		File f = new File(directory, id);
		return f;

	}

	private byte[] storeData(InputStream data, MessageDigest md)
			throws IOException {
		md.reset();
		byte[] buffer = new byte[bufferSize];
		int read = data.read(buffer);
		md.update(buffer, 0, read);
		if (read < buffer.length) {
			data.close();
			// small file, fits completely into the buffer
			byte[] id = md.digest();
			if (hasData(id))
				return id;
			File outputFile = getStorageFile(IdUtils.toString(id));
			File dir = outputFile.getParentFile();
			if (!dir.exists())
				dir.mkdirs();
			FileOutputStream fos = new FileOutputStream(outputFile);
			fos.write(buffer, 0, read);
			fos.close();
			outputFile.setReadOnly();
			return id;
		}

		File spool = File.createTempFile("buffer_", ".data", dataDirectory);
		OutputStream fos = new FileOutputStream(spool);
		boolean deflated = false;
		{
			// deflate the buffer, see if that works
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
			{
				DeflaterOutputStream def = new DeflaterOutputStream(baos);
				def.write(buffer, 0, read);
				def.close();
			}
			if (baos.size() < buffer.length - deflationTarget) {
				fos = new DeflaterOutputStream(fos);
				deflated = true;
			}
		}

		int count = read;
		fos.write(buffer, 0, read);
		while (true) {
			read = data.read(buffer);
			if (read <= 0)
				break;
			count += read;
			md.update(buffer, 0, read);
			fos.write(buffer, 0, read);
		}
		fos.close();
		data.close();
		byte[] id = md.digest();
		if (hasData(id)) {
			spool.delete();
			return id;
		}

		File outputFile = getStorageFile(IdUtils.toString(id));
		File sizeFile = null;
		if (deflated) {
			sizeFile = new File(outputFile.getParentFile(), outputFile
					.getName()
					+ ".size");
			outputFile = new File(outputFile.getParentFile(), outputFile
					.getName()
					+ ".deflated");
		}
		File dir = outputFile.getParentFile();
		if (!dir.exists())
			dir.mkdirs();
		if (!spool.renameTo(outputFile))
			throw new IOException("failed to move spool file " + spool + " to "
					+ outputFile);
		outputFile.setReadOnly();
		if (sizeFile != null) {
			fos = new FileOutputStream(sizeFile);
			fos.write(String.valueOf(count).getBytes());
			fos.close();
		}

		return id;
	}

	public long getSize(byte[] id) throws IOException {
		if (id == null)
			return 0;
		File storageFile = getStorageFile(IdUtils.toString(id));
		if (storageFile.exists())
			return storageFile.length();
		// compressed files have a ".size" file
		storageFile = new File(storageFile.getParentFile(), storageFile
				.getName()
				+ ".size");
		if (!storageFile.exists())
			return 0;
		FileInputStream fis = new FileInputStream(storageFile);
		byte[] size = new byte[32];
		int read = fis.read(size);
		fis.close();
		return Long.parseLong(new String(size, 0, read));
	}

	public long getDeflatedSize(byte[] id) throws IOException {
		File file = getStorageFile(IdUtils.toString(id));
		file = new File(file.getParentFile(), file.getName() + ".deflated");
		if (!file.exists())
			return 0;
		return file.length();
	}

	public InputStream readDeflatedData(byte[] id) throws IOException {
		File file = getStorageFile(IdUtils.toString(id));
		file = new File(file.getParentFile(), file.getName() + ".deflated");
		if (!file.exists())
			return null;
		return new FileInputStream(file);
	}

}

