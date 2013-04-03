package net.jeeeyul.eclipse.themes;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.progress.UIJob;

/**
 * Add "empty" class(CSS) into {@link CTabFolder} when there is no item.
 */
public class UpdateCTabFolderClassesJob extends UIJob {

	private CTabFolder folder;

	public UpdateCTabFolderClassesJob(CTabFolder folder) {
		super("Update CTabFolder CSS"); //$NON-NLS-1$
		this.folder = folder;
		this.setSystem(true);
	}

	@Override
	public IStatus runInUIThread(IProgressMonitor arg0) {
		if (folder == null || folder.isDisposed()) {
			return Status.OK_STATUS;
		}

		CSSClasses classes = CSSClasses.getStyleClasses(folder);
		boolean haveToSetEmpty = folder.getItemCount() == 0;

		if (haveToSetEmpty) {
			classes.add("empty"); //$NON-NLS-1$
			classes.remove("nonEmpty"); //$NON-NLS-1$
		} else {
			classes.remove("empty"); //$NON-NLS-1$
			classes.add("nonEmpty"); //$NON-NLS-1$
		}

		CSSClasses.setStyleClasses(folder, classes);
		getThemeEngine().applyStyles(folder, true);

		return Status.OK_STATUS;
	}

	private IThemeEngine getThemeEngine() {
		return (IThemeEngine) folder.getDisplay().getData("org.eclipse.e4.ui.css.swt.theme"); //$NON-NLS-1$
	}

	@Override
	public boolean shouldSchedule() {
		return folder != null && !folder.isDisposed();
	}

	@Override
	public boolean shouldRun() {
		return shouldSchedule();
	}
}
