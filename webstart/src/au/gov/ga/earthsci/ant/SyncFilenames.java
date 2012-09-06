package au.gov.ga.earthsci.ant;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class SyncFilenames extends Task
{
	private String fromdir;
	private String todir;
	private boolean keeppacked = false;

	public void setFromdir(String fromdir)
	{
		this.fromdir = fromdir;
	}

	public void setTodir(String todir)
	{
		this.todir = todir;
	}

	public void setKeeppacked(boolean keeppacked)
	{
		this.keeppacked = keeppacked;
	}

	@Override
	public void execute() throws BuildException
	{
		Set<String> fromdirFiles = new HashSet<String>();
		Set<String> todirFiles = new HashSet<String>();

		File src = new File(fromdir);
		File dst = new File(todir);

		if (src.isDirectory() && dst.isDirectory())
		{
			List<String> fromdirList = Arrays.asList(src.list());
			fromdirFiles.addAll(fromdirList);
			if (keeppacked)
			{
				for (String filename : fromdirList)
				{
					fromdirFiles.add(filename + ".pack.gz");
				}
			}

			todirFiles.addAll(Arrays.asList(dst.list()));

			todirFiles.removeAll(fromdirFiles);
			for (String filename : todirFiles)
			{
				File file = new File(dst, filename);
				System.out.println("Deleting file: " + file);
				file.delete();
			}
		}
	}
}
