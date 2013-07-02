package au.gov.ga.earthsci.eclipse.extras.ide;

public class IDE
{
	/**
	 * Preferences defined by the IDE workbench.
	 * <p>
	 * This interface is not intended to be implemented by clients.
	 * </p>
	 */
	public interface Preferences {

		/**
		 * A named preference for how a new perspective should be opened when a
		 * new project is created.
		 * <p>
		 * Value is of type <code>String</code>. The possible values are
		 * defined by the constants
		 * <code>OPEN_PERSPECTIVE_WINDOW, OPEN_PERSPECTIVE_PAGE, 
		 * OPEN_PERSPECTIVE_REPLACE, and NO_NEW_PERSPECTIVE</code>.
		 * </p>
		 * 
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_WINDOW
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_PAGE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#OPEN_PERSPECTIVE_REPLACE
		 * @see org.eclipse.ui.IWorkbenchPreferenceConstants#NO_NEW_PERSPECTIVE
		 */
		public static final String PROJECT_OPEN_NEW_PERSPECTIVE = "PROJECT_OPEN_NEW_PERSPECTIVE"; //$NON-NLS-1$

		/**
		 * <p>
		 * Specifies whether or not the workspace selection dialog should be
		 * shown on startup.
		 * </p>
		 * <p>
		 * The default value for this preference is <code>true</code>.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String SHOW_WORKSPACE_SELECTION_DIALOG = "SHOW_WORKSPACE_SELECTION_DIALOG"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores the maximum number of workspaces that should be displayed in
		 * the ChooseWorkspaceDialog.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String MAX_RECENT_WORKSPACES = "MAX_RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores a comma separated list of the recently used workspace paths.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String RECENT_WORKSPACES = "RECENT_WORKSPACES"; //$NON-NLS-1$

		/**
		 * <p>
		 * Stores the version of the protocol used to decode/encode the list of
		 * recent workspaces.
		 * </p>
		 * 
		 * @since 3.1
		 */
		public static final String RECENT_WORKSPACES_PROTOCOL = "RECENT_WORKSPACES_PROTOCOL"; //$NON-NLS-1$

	}
}
