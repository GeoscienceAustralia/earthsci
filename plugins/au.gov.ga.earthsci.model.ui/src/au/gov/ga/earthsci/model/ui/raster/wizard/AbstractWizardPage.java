package au.gov.ga.earthsci.model.ui.raster.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.common.util.Util;

/**
 * Base class for wizard pages
 * <p/>
 * Provides convenient methods for validation and data binding.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractWizardPage<P> extends WizardPage
{

	private static final Point DEFAULT_MIN_PAGE_SIZE = new Point(300, 300);

	private static final int GROUP_DESCRIPTION_TOP_GAP = 5;

	private static final String NORMAL_TEXT_COLOR = "normalText"; //$NON-NLS-1$
	private static final String ERROR_TEXT_COLOR = "errorText"; //$NON-NLS-1$

	/** The parameters being collected with this page */
	protected final P params;

	private final ColorRegistry colorRegistry;

	private List<Control> fields = new ArrayList<Control>();

	private final Listener validationListener = new Listener()
	{
		@Override
		public void handleEvent(Event event)
		{
			reValidate();
		}
	};

	private ScrolledComposite scroller;

	protected AbstractWizardPage(P params, String title, String description)
	{
		super(title);

		setTitle(title);
		setDescription(description);

		this.params = params;

		colorRegistry = new ColorRegistry(Display.getDefault(), true);

		colorRegistry.put(ERROR_TEXT_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_RED).getRGB());
		colorRegistry.put(NORMAL_TEXT_COLOR, Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB());
	}

	protected void reValidate()
	{
		clearValidation();
		validate();
		getWizard().getContainer().updateButtons();
	}

	@Override
	public void createControl(Composite parent)
	{
		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new GridLayout());

		scroller = new ScrolledComposite(root, SWT.H_SCROLL | SWT.V_SCROLL);
		scroller.setLayoutData(new GridData(GridData.FILL_BOTH));
		scroller.setExpandVertical(true);
		scroller.setExpandHorizontal(true);
		scroller.setMinSize(DEFAULT_MIN_PAGE_SIZE);

		Composite container = new Composite(scroller, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout());
		addContents(container);

		scroller.setContent(container);
		scroller.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				updateScrollerMinSize();
			}
		});

		setControl(root);
	}

	private void updateScrollerMinSize()
	{
		Rectangle r = scroller.getClientArea();
		Point computeSize = scroller.getContent().computeSize(r.width, SWT.DEFAULT);

		scroller.setMinSize(Math.max(DEFAULT_MIN_PAGE_SIZE.x, computeSize.x),
				Math.max(DEFAULT_MIN_PAGE_SIZE.y, computeSize.y));
	}

	/**
	 * Add the contents of the page to the the container.
	 * 
	 * @param container
	 *            The container to add contents to. Has a grid layout with 1
	 *            column.
	 */
	abstract void addContents(Composite container);

	/**
	 * Add a group to the given parent container with a given title and
	 * description.
	 * <p/>
	 * The returned group will have a Grid layout with 2 columns.
	 * 
	 * @return A {@link Group} with grid layout of 2 columns.
	 */
	protected Group addGroup(String title, String description, Composite parent)
	{
		return addGroup(title, description, parent, 2, true);
	}

	/**
	 * Add a group to the given parent container with a given title and
	 * description.
	 * <p/>
	 * The returned group will have a Grid layout with n columns.
	 * <p/>
	 * An optional vertical spacing can be added after the group description -
	 * recommended for "top-level" groupings.
	 * 
	 * @return A {@link Group} with grid layout of n columns.
	 */
	protected Group addGroup(String title, String description, Composite parent, int numColumns, boolean addSpacer)
	{
		Group result = new Group(parent, SWT.SHADOW_ETCHED_IN);
		result.setText(title);
		result.setFont(JFaceResources.getFontRegistry().getBold("default")); //$NON-NLS-1$
		result.setLayout(new GridLayout(numColumns, false));

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		result.setLayoutData(gd);

		Label groupDescription = new Label(result, SWT.WRAP);
		groupDescription.setText(description);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		gd.verticalIndent = GROUP_DESCRIPTION_TOP_GAP;
		groupDescription.setLayoutData(gd);

		if (addSpacer)
		{
			Label spacer = new Label(result, SWT.NONE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = numColumns;
			spacer.setLayoutData(gd);
		}

		return result;
	}

	/**
	 * Perform required validation on fields.
	 * <p/>
	 * Invalid fields should be marked as invalid using
	 * {@link #markInvalid(Control, String)}.
	 * 
	 */
	abstract void validate();

	/**
	 * Bind collected values to the backing parameters object
	 */
	public abstract void bind();

	/**
	 * Register a field to trigger validation on change
	 */
	protected void registerField(Control field)
	{
		field.addListener(SWT.SELECTED, validationListener);
		fields.add(field);
	}

	/**
	 * Remove validation state
	 */
	protected void clearValidation()
	{
		setErrorMessage(null);
		for (Control field : fields)
		{
			markValid(field);
		}
	}

	/**
	 * Mark the given control as valid.
	 * 
	 * @see #markInvalid(Control, String)
	 * 
	 * @param field
	 *            The control to mark as valid
	 */
	protected void markValid(Control field)
	{
		if (field instanceof Text)
		{
			((Text) field).setForeground(colorRegistry.get(NORMAL_TEXT_COLOR));
		}
	}

	/**
	 * Mark the given control as invalid. This will add the given message to the
	 * list of validation messages as appropriate.
	 * 
	 * @param field
	 *            The control to mark as invalid
	 * @param message
	 *            The validation message to associate with the control
	 */
	protected void markInvalid(Control field, String message)
	{
		if (field instanceof Text)
		{
			((Text) field).setForeground(colorRegistry.get(ERROR_TEXT_COLOR));
		}

		if (getErrorMessage() == null)
		{
			setErrorMessage(message);
		}
	}

	@Override
	public boolean canFlipToNextPage()
	{
		return getNextPage() != null && getErrorMessage() == null;
	}

	@Override
	public boolean isPageComplete()
	{
		return getErrorMessage() == null;
	}


	protected static Double getDoubleOrNull(String text)
	{
		if (Util.isEmpty(text))
		{
			return null;
		}
		return Double.valueOf(text);
	}

	protected static Integer getIntegerOrNull(String text)
	{
		if (Util.isEmpty(text))
		{
			return null;
		}
		return Integer.valueOf(text);
	}

	protected static boolean isIntegerOrEmpty(String text)
	{
		if (Util.isEmpty(text))
		{
			return true;
		}
		try
		{
			Integer.valueOf(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected static boolean isNumericOrEmpty(String text)
	{
		if (Util.isEmpty(text))
		{
			return true;
		}
		try
		{
			Double.valueOf(text);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
