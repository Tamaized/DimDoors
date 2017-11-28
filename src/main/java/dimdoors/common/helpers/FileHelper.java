package dimdoors.common.helpers;

import dimdoors.DimDoors;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {

	public static boolean copyFile(String ori, String dest) {
		try {
			//Note: For this to work properly, you must use getClass() on an instance of the class,
			//not on the value obtained from .class. That was what caused this code to fail before.
			//SchematicLoader didn't have this problem because we used instances of it.
			InputStream in = DimDoors.instance.getClass().getResourceAsStream(ori);
			OutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (Exception e) {
			DimDoors.LOGGER.info("Unable to get resource: " + ori);
			return false;
		}
		return true;
	}

}