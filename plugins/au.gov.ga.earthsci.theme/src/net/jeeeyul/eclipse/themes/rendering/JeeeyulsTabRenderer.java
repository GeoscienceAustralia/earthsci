package net.jeeeyul.eclipse.themes.rendering;

import com.google.common.base.Objects;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import net.jeeeyul.eclipse.themes.rendering.JTabSettings;
import net.jeeeyul.eclipse.themes.rendering.internal.EmptyClassHook;
import net.jeeeyul.eclipse.themes.rendering.internal.ImageDataUtil;
import net.jeeeyul.eclipse.themes.rendering.internal.JTabRendererHelper;
import net.jeeeyul.eclipse.themes.rendering.internal.Shadow9PatchFactory;
import net.jeeeyul.swtend.SWTExtensions;
import net.jeeeyul.swtend.sam.Procedure1;
import net.jeeeyul.swtend.ui.HSB;
import net.jeeeyul.swtend.ui.NinePatch;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.ExclusiveRange;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;

/**
 * A new CTabFolder Renderer for Jeeeyul's eclipse themes 2.0
 * 
 * @since 2.0
 */
@SuppressWarnings("all")
public class JeeeyulsTabRenderer extends CTabFolderRenderer {
  @Extension
  private JTabRendererHelper _jTabRendererHelper = new JTabRendererHelper();
  
  @Extension
  private SWTExtensions _sWTExtensions = SWTExtensions.INSTANCE;
  
  private boolean _debug = false;
  
  public boolean isDebug() {
    return this._debug;
  }
  
  public void setDebug(final boolean debug) {
    this._debug = debug;
  }
  
  private JTabSettings settings = new JTabSettings(this);
  
  private CTabFolder tabFolder;
  
  private NinePatch shadowNinePatch;
  
  private EmptyClassHook emptyClassHook;
  
  private PropertyChangeListener settingsListener = new PropertyChangeListener() {
    public void propertyChange(final PropertyChangeEvent it) {
      JeeeyulsTabRenderer.this.handleSettingChange(it);
    }
  };
  
  private Listener windowsRedrawHook = new Listener() {
    public void handleEvent(final Event it) {
      JeeeyulsTabRenderer.this.parent.redraw();
    }
  };
  
  private void handleSettingChange(final PropertyChangeEvent event) {
    String _propertyName = event.getPropertyName();
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(_propertyName, "shadow-color")) {
        _matched=true;
        this._sWTExtensions.<NinePatch>safeDispose(this.shadowNinePatch);
      }
    }
    if (!_matched) {
      if (Objects.equal(_propertyName, "shadow-radius")) {
        _matched=true;
        this._sWTExtensions.<NinePatch>safeDispose(this.shadowNinePatch);
      }
    }
    if (!_matched) {
      if (Objects.equal(_propertyName, "border-radius")) {
        _matched=true;
        this._sWTExtensions.<NinePatch>safeDispose(this.shadowNinePatch);
      }
    }
    this.tabFolder.redraw();
  }
  
  public JeeeyulsTabRenderer(final CTabFolder parent) {
    super(parent);
    this.tabFolder = parent;
    EmptyClassHook _emptyClassHook = new EmptyClassHook(parent);
    this.emptyClassHook = _emptyClassHook;
    this.settings.addPropertyChangeListener(this.settingsListener);
    boolean _isWindow = this._jTabRendererHelper.isWindow();
    if (_isWindow) {
      this.tabFolder.addListener(SWT.Resize, this.windowsRedrawHook);
    }
  }
  
  protected void dispose() {
    this._sWTExtensions.<NinePatch>safeDispose(this.shadowNinePatch);
    this.emptyClassHook.dispose();
    this.settings.removePropertyChangeListener(this.settingsListener);
    boolean _and = false;
    boolean _isWindow = this._jTabRendererHelper.isWindow();
    if (!_isWindow) {
      _and = false;
    } else {
      boolean _isAlive = this._sWTExtensions.isAlive(this.parent);
      _and = _isAlive;
    }
    if (_and) {
      this.parent.removeListener(SWT.Resize, this.windowsRedrawHook);
    }
    super.dispose();
  }
  
  protected Point computeSize(final int part, final int state, final GC gc, final int wHint, final int hHint) {
    Point _switchResult = null;
    boolean _matched = false;
    if (!_matched) {
      if ((part >= 0)) {
        _matched=true;
        CTabItem item = this.parent.getItem(part);
        int width = 0;
        int height = 0;
        width = (width + this.settings.getTabItemPaddings().x);
        Image _image = item.getImage();
        boolean _notEquals = (!Objects.equal(_image, null));
        if (_notEquals) {
          width = (width + item.getImage().getBounds().width);
          int _tabItemHorizontalSpacing = this.settings.getTabItemHorizontalSpacing();
          int _plus = (width + _tabItemHorizontalSpacing);
          width = _plus;
          Image _image_1 = item.getImage();
          Rectangle _bounds = _image_1.getBounds();
          height = _bounds.height;
        }
        String _text = item.getText();
        String _trim = _text.trim();
        int _length = _trim.length();
        boolean _greaterThan = (_length > 0);
        if (_greaterThan) {
          String _text_1 = item.getText();
          Font _font = item.getFont();
          Font _font_1 = this.parent.getFont();
          Font _firstNotNull = this._jTabRendererHelper.<Font>getFirstNotNull(Collections.<Font>unmodifiableList(Lists.<Font>newArrayList(_font, _font_1)));
          Point textSize = this._sWTExtensions.computeTextExtent(_text_1, _firstNotNull);
          width = ((width + textSize.x) + 1);
          int _max = Math.max(height, textSize.y);
          height = _max;
        }
        boolean _or = false;
        boolean _showClose = this._jTabRendererHelper.getShowClose(this.parent);
        if (_showClose) {
          _or = true;
        } else {
          boolean _showClose_1 = item.getShowClose();
          _or = _showClose_1;
        }
        if (_or) {
          boolean _or_1 = false;
          boolean _hasFlags = this._sWTExtensions.hasFlags(state, SWT.SELECTED);
          if (_hasFlags) {
            _or_1 = true;
          } else {
            boolean _showUnselectedClose = this._jTabRendererHelper.getShowUnselectedClose(this.parent);
            _or_1 = _showUnselectedClose;
          }
          if (_or_1) {
            Point closeButtonSize = this.computeSize(CTabFolderRenderer.PART_CLOSE_BUTTON, SWT.NONE, gc, SWT.DEFAULT, SWT.DEFAULT);
            int _tabItemHorizontalSpacing_1 = this.settings.getTabItemHorizontalSpacing();
            int _plus_1 = (width + _tabItemHorizontalSpacing_1);
            int _plus_2 = (_plus_1 + 1);
            width = _plus_2;
            width = (width + closeButtonSize.x);
            int _max_1 = Math.max(height, closeButtonSize.y);
            height = _max_1;
          }
        }
        Rectangle _tabItemPaddings = this.settings.getTabItemPaddings();
        int _max_2 = Math.max(_tabItemPaddings.width, 0);
        int _plus_3 = (width + _max_2);
        width = _plus_3;
        int _tabSpacing = this.settings.getTabSpacing();
        int _plus_4 = (width + _tabSpacing);
        width = _plus_4;
        return new Point(width, height);
      }
    }
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_HEADER)) {
        _matched=true;
        int _tabHeight = this.parent.getTabHeight();
        Point size = new Point(0, _tabHeight);
        int _itemCount = this.parent.getItemCount();
        boolean _equals = (_itemCount == 0);
        if (_equals) {
          Point _textExtent = gc.textExtent("Default");
          int _max_3 = Math.max(_textExtent.y, size.y);
          size.y = _max_3;
        } else {
          int _itemCount_1 = this.parent.getItemCount();
          ExclusiveRange _doubleDotLessThan = new ExclusiveRange(0, _itemCount_1, true);
          for (final Integer i : _doubleDotLessThan) {
            {
              Point eachSize = this.computeSize((i).intValue(), SWT.NONE, gc, wHint, hHint);
              int _max_4 = Math.max(size.y, eachSize.y);
              size.y = _max_4;
            }
          }
        }
        size.y = (size.y + 2);
        return size;
      }
    }
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_CLOSE_BUTTON)) {
        _matched=true;
        return new Point(11, 11);
      }
    }
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_CHEVRON_BUTTON)) {
        _matched=true;
        return new Point(20, 16);
      }
    }
    if (!_matched) {
      _switchResult = super.computeSize(part, state, gc, wHint, hHint);
    }
    return _switchResult;
  }
  
  protected Rectangle computeTrim(final int part, final int state, final int x, final int y, final int width, final int height) {
    Rectangle result = new Rectangle(x, y, width, height);
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_BODY)) {
        _matched=true;
        result.x = ((result.x - this.settings.getMargins().x) - this.settings.getPaddings().x);
        result.width = ((((result.width + this.settings.getMargins().x) + this.settings.getPaddings().x) + this.settings.getPaddings().width) + this.settings.getMargins().width);
        boolean _onBottom = this._jTabRendererHelper.getOnBottom(this.tabFolder);
        if (_onBottom) {
          throw new UnsupportedOperationException();
        }
        int _tabHeight = this.tabFolder.getTabHeight();
        int _minus = (result.y - _tabHeight);
        Rectangle _paddings = this.settings.getPaddings();
        int _minus_1 = (_minus - _paddings.y);
        int _minus_2 = (_minus_1 - 2);
        result.y = _minus_2;
        int _tabHeight_1 = this.tabFolder.getTabHeight();
        int _plus = (result.height + _tabHeight_1);
        Rectangle _paddings_1 = this.settings.getPaddings();
        int _plus_1 = (_plus + _paddings_1.y);
        Rectangle _margins = this.settings.getMargins();
        int _plus_2 = (_plus_1 + _margins.height);
        Rectangle _paddings_2 = this.settings.getPaddings();
        int _plus_3 = (_plus_2 + _paddings_2.height);
        int _plus_4 = (_plus_3 + 2);
        result.height = _plus_4;
        HSB[] _borderColors = this.settings.getBorderColors();
        boolean _notEquals = (!Objects.equal(_borderColors, null));
        if (_notEquals) {
          result.x = (result.x - 1);
          result.width = (result.width + 2);
          result.height = (result.height + 1);
        }
      }
    }
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_BACKGROUND)) {
        _matched=true;
        result.height = (result.height + 10);
      }
    }
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_BORDER)) {
        _matched=true;
        result.x = (result.x - this.settings.getMargins().x);
        int _borderRadius = this.settings.getBorderRadius();
        int _divide = (_borderRadius / 2);
        int _plus_5 = (((result.width + this.settings.getMargins().x) + this.settings.getMargins().width) + _divide);
        result.width = _plus_5;
      }
    }
    if (!_matched) {
      if (Objects.equal(part, CTabFolderRenderer.PART_HEADER)) {
        _matched=true;
        result.x = (result.x - this.settings.getMargins().x);
        result.width = ((result.width + this.settings.getMargins().x) + this.settings.getMargins().width);
      }
    }
    if (!_matched) {
      if ((((part == CTabFolderRenderer.PART_CHEVRON_BUTTON) || (part == CTabFolderRenderer.PART_MIN_BUTTON)) || (part == CTabFolderRenderer.PART_MAX_BUTTON))) {
        _matched=true;
      }
    }
    if (!_matched) {
      if ((part >= 0)) {
        _matched=true;
      }
    }
    if (!_matched) {
      Rectangle _computeTrim = super.computeTrim(part, state, x, y, width, height);
      result = _computeTrim;
    }
    return result;
  }
  
  protected void draw(final int part, final int state, final Rectangle bounds, final GC gc) {
    try {
      this.doDraw(part, state, bounds, gc);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        e.printStackTrace();
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  private Object doDraw(final int part, final int state, final Rectangle bounds, final GC gc) {
    Object _xblockexpression = null;
    {
      gc.setAdvanced(true);
      gc.setAntialias(SWT.ON);
      gc.setInterpolation(SWT.HIGH);
      gc.setLineJoin(SWT.JOIN_ROUND);
      gc.setAlpha(255);
      gc.setFillRule(SWT.FILL_WINDING);
      Color _background = this.tabFolder.getBackground();
      gc.setBackground(_background);
      Color _foreground = this.tabFolder.getForeground();
      gc.setForeground(_foreground);
      gc.setLineWidth(1);
      gc.setLineStyle(SWT.LINE_SOLID);
      Object _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        if (Objects.equal(part, CTabFolderRenderer.PART_HEADER)) {
          _matched=true;
          this.drawTabHeader(part, state, bounds, gc);
          this.updateChevronImage();
        }
      }
      if (!_matched) {
        if (Objects.equal(part, CTabFolderRenderer.PART_CLOSE_BUTTON)) {
          _matched=true;
          _switchResult = this.drawCloseButton(part, state, bounds, gc);
        }
      }
      if (!_matched) {
        if (Objects.equal(part, CTabFolderRenderer.PART_BORDER)) {
          _matched=true;
          _switchResult = null;
        }
      }
      if (!_matched) {
        if (Objects.equal(part, CTabFolderRenderer.PART_BODY)) {
          _matched=true;
          _switchResult = this.drawTabBody(part, state, bounds, gc);
        }
      }
      if (!_matched) {
        if (Objects.equal(part, CTabFolderRenderer.PART_CHEVRON_BUTTON)) {
          _matched=true;
          _switchResult = this.drawChevronButton(part, state, bounds, gc);
        }
      }
      if (!_matched) {
        if ((part >= 0)) {
          _matched=true;
          _switchResult = this.drawTabItem(part, state, bounds, gc);
        }
      }
      if (!_matched) {
        super.draw(part, state, bounds, gc);
      }
      _xblockexpression = _switchResult;
    }
    return _xblockexpression;
  }
  
  protected Object drawChevronButton(final int part, final int state, final Rectangle rectangle, final GC gc) {
    return null;
  }
  
  private void updateChevronImage() {
    ToolBar _chevron = this._jTabRendererHelper.getChevron(this.parent);
    final Point chevronSize = _chevron.getSize();
    if (((chevronSize.x == 0) || (chevronSize.y == 0))) {
      return;
    }
    ToolBar _chevron_1 = this._jTabRendererHelper.getChevron(this.parent);
    Object _data = _chevron_1.getData("last-render-color");
    HSB lastColor = ((HSB) _data);
    ToolBar _chevron_2 = this._jTabRendererHelper.getChevron(this.parent);
    Object _data_1 = _chevron_2.getData("last-render-count");
    Integer lastCount = ((Integer) _data_1);
    CTabItem[] _items = this.parent.getItems();
    final Function1<CTabItem, Boolean> _function = new Function1<CTabItem, Boolean>() {
      public Boolean apply(final CTabItem it) {
        boolean _isShowing = it.isShowing();
        return Boolean.valueOf((_isShowing == false));
      }
    };
    Iterable<CTabItem> _filter = IterableExtensions.<CTabItem>filter(((Iterable<CTabItem>)Conversions.doWrapArray(_items)), _function);
    int _size = IterableExtensions.size(_filter);
    final int count = Math.min(_size, 99);
    boolean _and = false;
    HSB _chevronColor = this.settings.getChevronColor();
    boolean _equals = Objects.equal(lastColor, _chevronColor);
    if (!_equals) {
      _and = false;
    } else {
      _and = ((lastCount).intValue() == count);
    }
    if (_and) {
      return;
    }
    Point size = this.computeSize(CTabFolderRenderer.PART_CHEVRON_BUTTON, SWT.NONE, null, SWT.DEFAULT, SWT.DEFAULT);
    ToolBar _chevron_3 = this._jTabRendererHelper.getChevron(this.parent);
    _chevron_3.setBackgroundImage(null);
    Display _display = this._sWTExtensions.getDisplay();
    final Image mask = new Image(_display, size.x, size.y);
    final GC mgc = new GC(mask);
    final Procedure1<Path> _function_1 = new Procedure1<Path>() {
      public void apply(final Path it) {
        it.moveTo(0, 0);
        it.lineTo(3, 3);
        it.lineTo(0, 6);
        it.moveTo(3, 0);
        it.lineTo(6, 3);
        it.lineTo(3, 6);
      }
    };
    Path path = this._sWTExtensions.newTemporaryPath(_function_1);
    Color _COLOR_BLACK = this._sWTExtensions.COLOR_BLACK();
    mgc.setBackground(_COLOR_BLACK);
    Rectangle _bounds = mask.getBounds();
    this._sWTExtensions.fill(mgc, _bounds);
    Color _COLOR_WHITE = this._sWTExtensions.COLOR_WHITE();
    mgc.setForeground(_COLOR_WHITE);
    this._sWTExtensions.draw(mgc, path);
    Font _font = this.parent.getFont();
    FontData[] _fontData = _font.getFontData();
    FontData fd = IterableExtensions.<FontData>head(((Iterable<FontData>)Conversions.doWrapArray(_fontData)));
    fd.setHeight(((72 * 10) / this._sWTExtensions.getDisplay().getDPI().y));
    Display _display_1 = this._sWTExtensions.getDisplay();
    Font _font_1 = new Font(_display_1, fd);
    Font _autoDispose = this._sWTExtensions.<Font>autoDispose(_font_1);
    mgc.setFont(_autoDispose);
    String _string = Integer.valueOf(count).toString();
    mgc.drawString(_string, 6, 5, true);
    mgc.dispose();
    ImageData _imageData = mask.getImageData();
    HSB _chevronColor_1 = this.settings.getChevronColor();
    ImageData data = ImageDataUtil.convertBrightnessToAlpha(_imageData, _chevronColor_1);
    mask.dispose();
    Display _display_2 = this._sWTExtensions.getDisplay();
    Image _image = new Image(_display_2, data);
    Image itemImage = this._sWTExtensions.<Image>shouldDisposeWith(_image, this.parent);
    Display _display_3 = this._sWTExtensions.getDisplay();
    Image _image_1 = new Image(_display_3, chevronSize.x, chevronSize.y);
    Image toolbarImg = this._sWTExtensions.<Image>shouldDisposeWith(_image_1, this.parent);
    GC tgc = new GC(toolbarImg);
    int _tabHeight = this.parent.getTabHeight();
    int _plus = (_tabHeight + 3);
    Rectangle _rectangle = new Rectangle(0, (-this._jTabRendererHelper.getChevron(this.parent).getBounds().y), chevronSize.x, _plus);
    Color[] _gradientColor = this._jTabRendererHelper.getGradientColor(this.parent);
    int[] _gradientPercents = this._jTabRendererHelper.getGradientPercents(this.parent);
    this._sWTExtensions.fillGradientRectangle(tgc, _rectangle, _gradientColor, _gradientPercents, true);
    tgc.dispose();
    ToolBar _chevron_4 = this._jTabRendererHelper.getChevron(this.parent);
    Image _backgroundImage = _chevron_4.getBackgroundImage();
    this._sWTExtensions.<Image>safeDispose(_backgroundImage);
    ToolBar _chevron_5 = this._jTabRendererHelper.getChevron(this.parent);
    _chevron_5.setBackgroundImage(null);
    ToolBar _chevron_6 = this._jTabRendererHelper.getChevron(this.parent);
    _chevron_6.setBackgroundImage(toolbarImg);
    ToolItem _chevronItem = this._jTabRendererHelper.getChevronItem(this.parent);
    Image _image_2 = _chevronItem.getImage();
    this._sWTExtensions.<Image>safeDispose(_image_2);
    ToolItem _chevronItem_1 = this._jTabRendererHelper.getChevronItem(this.parent);
    _chevronItem_1.setImage(null);
    ToolItem _chevronItem_2 = this._jTabRendererHelper.getChevronItem(this.parent);
    _chevronItem_2.setImage(itemImage);
    ToolBar _chevron_7 = this._jTabRendererHelper.getChevron(this.parent);
    HSB _chevronColor_2 = this.settings.getChevronColor();
    _chevron_7.setData("last-render-color", _chevronColor_2);
    ToolBar _chevron_8 = this._jTabRendererHelper.getChevron(this.parent);
    _chevron_8.setData("last-render-count", Integer.valueOf(count));
  }
  
  private Rectangle getHeaderArea() {
    Rectangle _xifexpression = null;
    boolean _onTop = this._jTabRendererHelper.getOnTop(this.tabFolder);
    if (_onTop) {
      Rectangle _margins = this.settings.getMargins();
      int _tabHeight = this.tabFolder.getTabHeight();
      int _plus = (_tabHeight + 2);
      _xifexpression = new Rectangle(_margins.x, 0, ((this.tabFolder.getSize().x - this.settings.getMargins().x) - this.settings.getMargins().width), _plus);
    } else {
      Point _size = this.tabFolder.getSize();
      Point _size_1 = this.tabFolder.getSize();
      Point _size_2 = this.tabFolder.getSize();
      Rectangle _rectangle = new Rectangle(0, _size.y, _size_1.x, _size_2.y);
      final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Rectangle> _function = new org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Rectangle>() {
        public void apply(final Rectangle it) {
          Rectangle _margins = JeeeyulsTabRenderer.this.settings.getMargins();
          Rectangle _margins_1 = JeeeyulsTabRenderer.this.settings.getMargins();
          JeeeyulsTabRenderer.this._sWTExtensions.shrink(it, _margins.x, 0, _margins_1.width, 0);
          int _tabHeight = JeeeyulsTabRenderer.this.tabFolder.getTabHeight();
          it.height = _tabHeight;
          int _tabHeight_1 = JeeeyulsTabRenderer.this.tabFolder.getTabHeight();
          int _minus = (-_tabHeight_1);
          Rectangle _margins_2 = JeeeyulsTabRenderer.this.settings.getMargins();
          int _minus_1 = (_minus - _margins_2.height);
          int _minus_2 = (_minus_1 - 2);
          JeeeyulsTabRenderer.this._sWTExtensions.translate(it, 0, _minus_2);
          JeeeyulsTabRenderer.this._sWTExtensions.resize(it, 0, 2);
        }
      };
      _xifexpression = ObjectExtensions.<Rectangle>operator_doubleArrow(_rectangle, _function);
    }
    Rectangle headerArea = _xifexpression;
    return headerArea;
  }
  
  protected GC drawTabHeader(final int part, final int state, final Rectangle bounds, final GC gc) {
    GC _xblockexpression = null;
    {
      boolean _onBottom = this._jTabRendererHelper.getOnBottom(this.parent);
      if (_onBottom) {
        throw new UnsupportedOperationException();
      }
      final Rectangle fillArea = this.getHeaderArea();
      HSB[] _borderColors = this.settings.getBorderColors();
      boolean _notEquals = (!Objects.equal(_borderColors, null));
      if (_notEquals) {
        this._sWTExtensions.shrink(fillArea, 1, 0);
      }
      Color[] _gradientColor = this._jTabRendererHelper.getGradientColor(this.tabFolder);
      boolean _notEquals_1 = (!Objects.equal(_gradientColor, null));
      if (_notEquals_1) {
        int _borderRadius = this.settings.getBorderRadius();
        Color[] _gradientColor_1 = this._jTabRendererHelper.getGradientColor(this.tabFolder);
        int[] _gradientPercents = this._jTabRendererHelper.getGradientPercents(this.tabFolder);
        this._sWTExtensions.fillGradientRoundRectangle(gc, fillArea, _borderRadius, this._sWTExtensions.CORNER_TOP, _gradientColor_1, _gradientPercents, true);
      } else {
        Color _background = this.tabFolder.getBackground();
        gc.setBackground(_background);
        int _borderRadius_1 = this.settings.getBorderRadius();
        this._sWTExtensions.fillRoundRectangle(gc, fillArea, _borderRadius_1, this._sWTExtensions.CORNER_TOP);
      }
      Rectangle _headerArea = this.getHeaderArea();
      final Rectangle outlineArea = this._sWTExtensions.getResized(_headerArea, (-1), 0);
      GC _xifexpression = null;
      HSB[] _borderColors_1 = this.settings.getBorderColors();
      boolean _notEquals_2 = (!Objects.equal(_borderColors_1, null));
      if (_notEquals_2) {
        GC _xblockexpression_1 = null;
        {
          HSB[] _borderColors_2 = this.settings.getBorderColors();
          HSB _head = IterableExtensions.<HSB>head(((Iterable<HSB>)Conversions.doWrapArray(_borderColors_2)));
          Color _autoDisposeColor = this._sWTExtensions.toAutoDisposeColor(_head);
          gc.setForeground(_autoDisposeColor);
          final Procedure1<Path> _function = new Procedure1<Path>() {
            public void apply(final Path it) {
              boolean _simple = JeeeyulsTabRenderer.this.parent.getSimple();
              if (_simple) {
                Point _bottomLeft = JeeeyulsTabRenderer.this._sWTExtensions.getBottomLeft(outlineArea);
                JeeeyulsTabRenderer.this._sWTExtensions.moveTo(it, _bottomLeft);
                Point _topLeft = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(outlineArea);
                int _borderRadius = JeeeyulsTabRenderer.this.settings.getBorderRadius();
                Point _translated = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_topLeft, 0, _borderRadius);
                JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _translated);
                int _borderRadius_1 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
                int _multiply = (_borderRadius_1 * 2);
                Rectangle _newRectangleWithSize = JeeeyulsTabRenderer.this._sWTExtensions.newRectangleWithSize(_multiply);
                Rectangle _relocateTopLeftWith = JeeeyulsTabRenderer.this._sWTExtensions.relocateTopLeftWith(_newRectangleWithSize, outlineArea);
                JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, _relocateTopLeftWith, 180, (-90));
              } else {
                Point _topLeft_1 = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(outlineArea);
                int _borderRadius_2 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
                Point _translated_1 = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_topLeft_1, _borderRadius_2, 0);
                JeeeyulsTabRenderer.this._sWTExtensions.moveTo(it, _translated_1);
              }
              Point _topRight = JeeeyulsTabRenderer.this._sWTExtensions.getTopRight(outlineArea);
              int _borderRadius_3 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
              int _minus = (-_borderRadius_3);
              Point _translated_2 = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_topRight, _minus, 0);
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _translated_2);
              int _borderRadius_4 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
              int _multiply_1 = (_borderRadius_4 * 2);
              Rectangle _newRectangleWithSize_1 = JeeeyulsTabRenderer.this._sWTExtensions.newRectangleWithSize(_multiply_1);
              Rectangle _relocateTopRightWith = JeeeyulsTabRenderer.this._sWTExtensions.relocateTopRightWith(_newRectangleWithSize_1, outlineArea);
              JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, _relocateTopRightWith, 90, (-90));
              Point _bottomRight = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(outlineArea);
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _bottomRight);
            }
          };
          Path path = this._sWTExtensions.newTemporaryPath(_function);
          HSB[] _borderColors_3 = this.settings.getBorderColors();
          Color[] _autoDisposeColors = this._sWTExtensions.toAutoDisposeColors(_borderColors_3);
          int[] _borderPercents = this.settings.getBorderPercents();
          _xblockexpression_1 = this._sWTExtensions.drawGradientPath(gc, path, _autoDisposeColors, _borderPercents, true);
        }
        _xifexpression = _xblockexpression_1;
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  protected GC drawTabBody(final int part, final int state, final Rectangle bounds, final GC gc) {
    GC _xblockexpression = null;
    {
      boolean _onBottom = this._jTabRendererHelper.getOnBottom(this.parent);
      if (_onBottom) {
        throw new UnsupportedOperationException();
      }
      Composite _parent = this.tabFolder.getParent();
      Color _background = _parent.getBackground();
      gc.setBackground(_background);
      this._sWTExtensions.fill(gc, bounds);
      HSB _shadowColor = this.settings.getShadowColor();
      boolean _notEquals = (!Objects.equal(_shadowColor, null));
      if (_notEquals) {
        this.drawShadow(part, state, bounds, gc);
      }
      Color _xifexpression = null;
      int _itemCount = this.parent.getItemCount();
      boolean _greaterThan = (_itemCount > 0);
      if (_greaterThan) {
        Color[] _selectionGradientColor = this._jTabRendererHelper.getSelectionGradientColor(this.tabFolder);
        Color _last = null;
        if (((Iterable<Color>)Conversions.doWrapArray(_selectionGradientColor))!=null) {
          _last=IterableExtensions.<Color>last(((Iterable<Color>)Conversions.doWrapArray(_selectionGradientColor)));
        }
        Color _selectionBackground = this.tabFolder.getSelectionBackground();
        final Function1<Color, Boolean> _function = new Function1<Color, Boolean>() {
          public Boolean apply(final Color it) {
            return Boolean.valueOf((!Objects.equal(it, null)));
          }
        };
        _xifexpression = IterableExtensions.<Color>findFirst(Collections.<Color>unmodifiableList(Lists.<Color>newArrayList(_last, _selectionBackground)), _function);
      } else {
        Color[] _gradientColor = this._jTabRendererHelper.getGradientColor(this.tabFolder);
        Color _last_1 = null;
        if (((Iterable<Color>)Conversions.doWrapArray(_gradientColor))!=null) {
          _last_1=IterableExtensions.<Color>last(((Iterable<Color>)Conversions.doWrapArray(_gradientColor)));
        }
        Color _background_1 = this.tabFolder.getBackground();
        final Function1<Color, Boolean> _function_1 = new Function1<Color, Boolean>() {
          public Boolean apply(final Color it) {
            return Boolean.valueOf((!Objects.equal(it, null)));
          }
        };
        _xifexpression = IterableExtensions.<Color>findFirst(Collections.<Color>unmodifiableList(Lists.<Color>newArrayList(_last_1, _background_1)), _function_1);
      }
      gc.setBackground(_xifexpression);
      Rectangle fillArea = this.tabArea();
      Rectangle _headerArea = this.getHeaderArea();
      Point _bottom = this._sWTExtensions.getBottom(_headerArea);
      this._sWTExtensions.setTop(fillArea, _bottom.y);
      int _borderRadius = this.settings.getBorderRadius();
      this._sWTExtensions.fillRoundRectangle(gc, fillArea, _borderRadius, this._sWTExtensions.CORNER_BOTTOM);
      Rectangle _paddings = this.settings.getPaddings();
      this._sWTExtensions.shrink(fillArea, _paddings);
      HSB[] _borderColors = this.settings.getBorderColors();
      boolean _notEquals_1 = (!Objects.equal(_borderColors, null));
      if (_notEquals_1) {
        this._sWTExtensions.shrink(fillArea, 1, 0, 1, 1);
      }
      Color _background_2 = this.parent.getBackground();
      gc.setBackground(_background_2);
      this._sWTExtensions.fill(gc, fillArea);
      GC _xifexpression_1 = null;
      boolean _and = false;
      boolean _and_1 = false;
      int _borderWidth = this.settings.getBorderWidth();
      boolean _greaterThan_1 = (_borderWidth > 0);
      if (!_greaterThan_1) {
        _and_1 = false;
      } else {
        HSB[] _borderColors_1 = this.settings.getBorderColors();
        boolean _notEquals_2 = (!Objects.equal(_borderColors_1, null));
        _and_1 = _notEquals_2;
      }
      if (!_and_1) {
        _and = false;
      } else {
        int[] _borderPercents = this.settings.getBorderPercents();
        boolean _notEquals_3 = (!Objects.equal(_borderPercents, null));
        _and = _notEquals_3;
      }
      if (_and) {
        GC _xblockexpression_1 = null;
        {
          Rectangle _tabArea = this.tabArea();
          final Rectangle offset = this._sWTExtensions.getResized(_tabArea, (-1), (-1));
          int _borderWidth_1 = this.settings.getBorderWidth();
          gc.setLineWidth(_borderWidth_1);
          final Procedure1<Path> _function_2 = new Procedure1<Path>() {
            public void apply(final Path it) {
              int _borderRadius = JeeeyulsTabRenderer.this.settings.getBorderRadius();
              int _multiply = (_borderRadius * 2);
              Rectangle corner = JeeeyulsTabRenderer.this._sWTExtensions.newRectangleWithSize(_multiply);
              Rectangle _headerArea = JeeeyulsTabRenderer.this.getHeaderArea();
              Point _bottomLeft = JeeeyulsTabRenderer.this._sWTExtensions.getBottomLeft(_headerArea);
              JeeeyulsTabRenderer.this._sWTExtensions.moveTo(it, _bottomLeft);
              JeeeyulsTabRenderer.this._sWTExtensions.relocateBottomLeftWith(corner, offset);
              Point _left = JeeeyulsTabRenderer.this._sWTExtensions.getLeft(corner);
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _left);
              JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, corner, 180, 90);
              JeeeyulsTabRenderer.this._sWTExtensions.relocateBottomRightWith(corner, offset);
              Point _bottom = JeeeyulsTabRenderer.this._sWTExtensions.getBottom(corner);
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _bottom);
              JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, corner, 270, 90);
              Rectangle _headerArea_1 = JeeeyulsTabRenderer.this.getHeaderArea();
              Point _bottomRight = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(_headerArea_1);
              Point _translated = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_bottomRight, (-1), 0);
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _translated);
            }
          };
          final Path bodyPath = this._sWTExtensions.newTemporaryPath(_function_2);
          HSB[] _borderColors_2 = this.settings.getBorderColors();
          HSB _last_2 = IterableExtensions.<HSB>last(((Iterable<HSB>)Conversions.doWrapArray(_borderColors_2)));
          Color _autoReleaseColor = this._sWTExtensions.toAutoReleaseColor(_last_2);
          gc.setForeground(_autoReleaseColor);
          this._sWTExtensions.draw(gc, bodyPath);
          _xblockexpression_1 = this._sWTExtensions.draw(gc, bodyPath);
        }
        _xifexpression_1 = _xblockexpression_1;
      }
      _xblockexpression = _xifexpression_1;
    }
    return _xblockexpression;
  }
  
  protected GC drawCloseButton(final int part, final int state, final Rectangle bounds, final GC gc) {
    GC _xblockexpression = null;
    {
      boolean _isDebug = this.isDebug();
      if (_isDebug) {
        Color _COLOR_MAGENTA = this._sWTExtensions.COLOR_MAGENTA();
        gc.setBackground(_COLOR_MAGENTA);
        this._sWTExtensions.fill(gc, bounds);
      }
      final Rectangle box = this._sWTExtensions.getShrinked(bounds, 2);
      final Procedure1<Path> _function = new Procedure1<Path>() {
        public void apply(final Path it) {
          Point _topLeft = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(box);
          JeeeyulsTabRenderer.this._sWTExtensions.moveTo(it, _topLeft);
          Point _bottomRight = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(box);
          JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _bottomRight);
          Point _topRight = JeeeyulsTabRenderer.this._sWTExtensions.getTopRight(box);
          JeeeyulsTabRenderer.this._sWTExtensions.moveTo(it, _topRight);
          Point _bottomLeft = JeeeyulsTabRenderer.this._sWTExtensions.getBottomLeft(box);
          JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _bottomLeft);
        }
      };
      Path path = this._sWTExtensions.newTemporaryPath(_function);
      HSB _switchResult = null;
      boolean _matched = false;
      if (!_matched) {
        boolean _hasFlags = this._sWTExtensions.hasFlags(state, SWT.HOT);
        if (_hasFlags) {
          _matched=true;
          HSB _closeButtonHotColor = this.settings.getCloseButtonHotColor();
          HSB _closeButtonColor = this.settings.getCloseButtonColor();
          _switchResult = this._jTabRendererHelper.<HSB>getFirstNotNull(Collections.<HSB>unmodifiableList(Lists.<HSB>newArrayList(_closeButtonHotColor, _closeButtonColor, HSB.BLACK)));
        }
      }
      if (!_matched) {
        boolean _hasFlags_1 = this._sWTExtensions.hasFlags(state, SWT.SELECTED);
        if (_hasFlags_1) {
          _matched=true;
          HSB _closeButtonActiveColor = this.settings.getCloseButtonActiveColor();
          HSB _closeButtonColor_1 = this.settings.getCloseButtonColor();
          _switchResult = this._jTabRendererHelper.<HSB>getFirstNotNull(Collections.<HSB>unmodifiableList(Lists.<HSB>newArrayList(_closeButtonActiveColor, _closeButtonColor_1, HSB.BLACK)));
        }
      }
      if (!_matched) {
        HSB _closeButtonColor_2 = this.settings.getCloseButtonColor();
        _switchResult = this._jTabRendererHelper.<HSB>getFirstNotNull(Collections.<HSB>unmodifiableList(Lists.<HSB>newArrayList(_closeButtonColor_2, HSB.BLACK)));
      }
      HSB color = _switchResult;
      int _closeButtonLineWidth = this.settings.getCloseButtonLineWidth();
      int _max = Math.max(_closeButtonLineWidth, 1);
      gc.setLineWidth(_max);
      Color _autoReleaseColor = this._sWTExtensions.toAutoReleaseColor(color);
      gc.setForeground(_autoReleaseColor);
      _xblockexpression = this._sWTExtensions.draw(gc, path);
    }
    return _xblockexpression;
  }
  
  protected GC drawTabItem(final int part, final int state, final Rectangle bounds, final GC gc) {
    GC _xblockexpression = null;
    {
      final CTabItem item = this.tabFolder.getItem(part);
      Rectangle _xifexpression = null;
      boolean _onBottom = this._jTabRendererHelper.getOnBottom(this.tabFolder);
      if (_onBottom) {
        throw new UnsupportedOperationException();
      } else {
        Rectangle _bounds = item.getBounds();
        int _tabSpacing = this.settings.getTabSpacing();
        int _max = Math.max(_tabSpacing, 0);
        int _minus = (-_max);
        _xifexpression = this._sWTExtensions.getResized(_bounds, _minus, 0);
      }
      final Rectangle itemBounds = _xifexpression;
      int _tabSpacing_1 = this.settings.getTabSpacing();
      boolean _equals = (_tabSpacing_1 == (-1));
      if (_equals) {
        this._sWTExtensions.resize(itemBounds, 1, 0);
      }
      this.drawTabItemBackground(part, state, itemBounds, gc);
      Rectangle _xifexpression_1 = null;
      Image _image = item.getImage();
      boolean _notEquals = (!Objects.equal(_image, null));
      if (_notEquals) {
        Image _image_1 = item.getImage();
        Rectangle _bounds_1 = _image_1.getBounds();
        Rectangle _bounds_2 = item.getBounds();
        Rectangle _relocateLeftWith = this._sWTExtensions.relocateLeftWith(_bounds_1, _bounds_2);
        Rectangle _tabItemPaddings = this.settings.getTabItemPaddings();
        _xifexpression_1 = this._sWTExtensions.translate(_relocateLeftWith, _tabItemPaddings.x, 0);
      } else {
        _xifexpression_1 = new Rectangle((itemBounds.x + this.settings.getTabItemPaddings().x), 0, 0, itemBounds.height);
      }
      Rectangle iconArea = _xifexpression_1;
      Image _image_2 = item.getImage();
      boolean _notEquals_1 = (!Objects.equal(_image_2, null));
      if (_notEquals_1) {
        Image _image_3 = item.getImage();
        Point _topLeft = this._sWTExtensions.getTopLeft(iconArea);
        this._sWTExtensions.drawImage(gc, _image_3, _topLeft);
      }
      boolean _and = false;
      boolean _or = false;
      boolean _showClose = this._jTabRendererHelper.getShowClose(this.tabFolder);
      if (_showClose) {
        _or = true;
      } else {
        boolean _showClose_1 = item.getShowClose();
        _or = _showClose_1;
      }
      if (!_or) {
        _and = false;
      } else {
        _and = (this._jTabRendererHelper.getCloseRect(item).width > 0);
      }
      if (_and) {
        Rectangle _closeRect = this._jTabRendererHelper.getCloseRect(item);
        _closeRect.x = (((this._sWTExtensions.getRight(item.getBounds()).x - this._jTabRendererHelper.getCloseRect(item).width) - 4) - this.settings.getTabItemPaddings().width);
        Rectangle _closeRect_1 = this._jTabRendererHelper.getCloseRect(item);
        int _tabSpacing_2 = this.settings.getTabSpacing();
        int _max_1 = Math.max(_tabSpacing_2, 0);
        int _minus_1 = (-_max_1);
        int _plus = (_minus_1 + 3);
        this._sWTExtensions.translate(_closeRect_1, _plus, 0);
        boolean _or_1 = false;
        boolean _hasFlags = this._sWTExtensions.hasFlags(state, SWT.SELECTED);
        if (_hasFlags) {
          _or_1 = true;
        } else {
          boolean _hasFlags_1 = this._sWTExtensions.hasFlags(state, SWT.HOT);
          _or_1 = _hasFlags_1;
        }
        if (_or_1) {
          Rectangle _closeRect_2 = this._jTabRendererHelper.getCloseRect(item);
          final Procedure1<GC> _function = new Procedure1<GC>() {
            public void apply(final GC it) {
              int _closeImageState = JeeeyulsTabRenderer.this._jTabRendererHelper.getCloseImageState(item);
              Rectangle _closeRect = JeeeyulsTabRenderer.this._jTabRendererHelper.getCloseRect(item);
              JeeeyulsTabRenderer.this.draw(CTabFolderRenderer.PART_CLOSE_BUTTON, _closeImageState, _closeRect, gc);
            }
          };
          this._sWTExtensions.withClip(gc, _closeRect_2, _function);
        }
      }
      gc.setLineWidth(1);
      Font _xifexpression_2 = null;
      Font _font = item.getFont();
      boolean _notEquals_2 = (!Objects.equal(_font, null));
      if (_notEquals_2) {
        _xifexpression_2 = item.getFont();
      } else {
        _xifexpression_2 = this.tabFolder.getFont();
      }
      gc.setFont(_xifexpression_2);
      String _text = item.getText();
      Font _font_1 = gc.getFont();
      final Point textSize = this._sWTExtensions.computeTextExtent(_text, _font_1);
      Rectangle _newRectangleWithSize = this._sWTExtensions.newRectangleWithSize(textSize);
      Point _right = this._sWTExtensions.getRight(iconArea);
      final Rectangle textArea = this._sWTExtensions.relocateLeftWith(_newRectangleWithSize, _right);
      Image _image_4 = item.getImage();
      boolean _notEquals_3 = (!Objects.equal(_image_4, null));
      if (_notEquals_3) {
        int _tabItemHorizontalSpacing = this.settings.getTabItemHorizontalSpacing();
        this._sWTExtensions.translate(textArea, _tabItemHorizontalSpacing, 0);
      }
      boolean _and_1 = false;
      boolean _and_2 = false;
      boolean _or_2 = false;
      boolean _showClose_2 = item.getShowClose();
      if (_showClose_2) {
        _or_2 = true;
      } else {
        boolean _showClose_3 = this._jTabRendererHelper.getShowClose(this.tabFolder);
        _or_2 = _showClose_3;
      }
      if (!_or_2) {
        _and_2 = false;
      } else {
        Rectangle _closeRect_3 = this._jTabRendererHelper.getCloseRect(item);
        boolean _notEquals_4 = (!Objects.equal(_closeRect_3, null));
        _and_2 = _notEquals_4;
      }
      if (!_and_2) {
        _and_1 = false;
      } else {
        _and_1 = (this._jTabRendererHelper.getCloseRect(item).width > 0);
      }
      if (_and_1) {
        Rectangle _closeRect_4 = this._jTabRendererHelper.getCloseRect(item);
        int _tabItemHorizontalSpacing_1 = this.settings.getTabItemHorizontalSpacing();
        int _minus_2 = (_closeRect_4.x - _tabItemHorizontalSpacing_1);
        this._sWTExtensions.setRight(textArea, _minus_2);
      } else {
        this._sWTExtensions.setRight(textArea, (this._sWTExtensions.getRight(itemBounds).x - this.settings.getTabItemPaddings().width));
      }
      boolean _isDebug = this.isDebug();
      if (_isDebug) {
        Color _COLOR_MAGENTA = this._sWTExtensions.COLOR_MAGENTA();
        gc.setForeground(_COLOR_MAGENTA);
        gc.setLineWidth(1);
        Rectangle _resized = this._sWTExtensions.getResized(textArea, (-1), (-1));
        this._sWTExtensions.draw(gc, _resized);
        Point _left = this._sWTExtensions.getLeft(textArea);
        Point _left_1 = this._sWTExtensions.getLeft(textArea);
        Point _translated = this._sWTExtensions.getTranslated(_left_1, textSize.x, 0);
        this._sWTExtensions.drawLine(gc, _left, _translated);
      }
      final Procedure1<GC> _function_1 = new Procedure1<GC>() {
        public void apply(final GC it) {
          String _xifexpression = null;
          if ((textSize.x > textArea.width)) {
            String _text = item.getText();
            _xifexpression = JeeeyulsTabRenderer.this._sWTExtensions.shortenText(gc, _text, textArea.width, "...");
          } else {
            _xifexpression = item.getText();
          }
          String text = _xifexpression;
          final HSB textShadowColor = JeeeyulsTabRenderer.this._jTabRendererHelper.getTextShadowColorFor(JeeeyulsTabRenderer.this.settings, state);
          final Point textShadowPosition = JeeeyulsTabRenderer.this._jTabRendererHelper.getTextShadowPositionFor(JeeeyulsTabRenderer.this.settings, state);
          boolean _and = false;
          boolean _and_1 = false;
          boolean _notEquals = (!Objects.equal(textShadowColor, null));
          if (!_notEquals) {
            _and_1 = false;
          } else {
            boolean _notEquals_1 = (!Objects.equal(textShadowPosition, null));
            _and_1 = _notEquals_1;
          }
          if (!_and_1) {
            _and = false;
          } else {
            boolean _isEmpty = JeeeyulsTabRenderer.this._sWTExtensions.isEmpty(textShadowPosition);
            boolean _not = (!_isEmpty);
            _and = _not;
          }
          if (_and) {
            Point shadowPosition = JeeeyulsTabRenderer.this._jTabRendererHelper.getTextShadowPositionFor(JeeeyulsTabRenderer.this.settings, state);
            HSB _textShadowColorFor = JeeeyulsTabRenderer.this._jTabRendererHelper.getTextShadowColorFor(JeeeyulsTabRenderer.this.settings, state);
            Color _autoReleaseColor = JeeeyulsTabRenderer.this._sWTExtensions.toAutoReleaseColor(_textShadowColorFor);
            gc.setForeground(_autoReleaseColor);
            Point _topLeft = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(textArea);
            Point _translated = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_topLeft, shadowPosition);
            JeeeyulsTabRenderer.this._sWTExtensions.drawString(gc, text, _translated);
          }
          HSB _textColorFor = JeeeyulsTabRenderer.this._jTabRendererHelper.getTextColorFor(JeeeyulsTabRenderer.this.settings, state);
          Color _autoReleaseColor_1 = JeeeyulsTabRenderer.this._sWTExtensions.toAutoReleaseColor(_textColorFor);
          gc.setForeground(_autoReleaseColor_1);
          Point _topLeft_1 = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(textArea);
          JeeeyulsTabRenderer.this._sWTExtensions.drawString(gc, text, _topLeft_1);
        }
      };
      this._sWTExtensions.withClip(gc, textArea, _function_1);
      this.drawTabItemBorder(part, state, itemBounds, gc);
      GC _xifexpression_3 = null;
      HSB[] _borderColors = this.settings.getBorderColors();
      boolean _notEquals_5 = (!Objects.equal(_borderColors, null));
      if (_notEquals_5) {
        GC _xifexpression_4 = null;
        CTabItem _firstVisibleItem = this._jTabRendererHelper.getFirstVisibleItem(this.parent);
        boolean _equals_1 = Objects.equal(_firstVisibleItem, item);
        if (_equals_1) {
          GC _xblockexpression_1 = null;
          {
            final Procedure1<Path> _function_2 = new Procedure1<Path>() {
              public void apply(final Path it) {
                Point _topRight = JeeeyulsTabRenderer.this._sWTExtensions.getTopRight(itemBounds);
                int _tabSpacing = JeeeyulsTabRenderer.this.settings.getTabSpacing();
                int _max = Math.max(_tabSpacing, 0);
                int _plus = (_max + 1);
                Point _translated = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_topRight, _plus, 0);
                JeeeyulsTabRenderer.this._sWTExtensions.moveTo(it, _translated);
                Point _topLeft = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(itemBounds);
                int _borderRadius = JeeeyulsTabRenderer.this.settings.getBorderRadius();
                Point _translated_1 = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_topLeft, _borderRadius, 0);
                JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _translated_1);
                int _borderRadius_1 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
                int _multiply = (_borderRadius_1 * 2);
                int _borderRadius_2 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
                int _multiply_1 = (_borderRadius_2 * 2);
                Rectangle _newRectangle = JeeeyulsTabRenderer.this._sWTExtensions.newRectangle(itemBounds.x, itemBounds.y, _multiply, _multiply_1);
                JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, _newRectangle, 90, 90);
                Point _bottomLeft = JeeeyulsTabRenderer.this._sWTExtensions.getBottomLeft(itemBounds);
                Point _translated_2 = JeeeyulsTabRenderer.this._sWTExtensions.getTranslated(_bottomLeft, 0, 1);
                JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _translated_2);
              }
            };
            Path path = this._sWTExtensions.newTemporaryPath(_function_2);
            HSB[] _borderColors_1 = this.settings.getBorderColors();
            Color[] _autoDisposeColors = this._sWTExtensions.toAutoDisposeColors(_borderColors_1);
            int[] _borderPercents = this.settings.getBorderPercents();
            _xblockexpression_1 = this._sWTExtensions.drawGradientPath(gc, path, _autoDisposeColors, _borderPercents, true);
          }
          _xifexpression_4 = _xblockexpression_1;
        } else {
          GC _xblockexpression_2 = null;
          {
            HSB[] _borderColors_1 = this.settings.getBorderColors();
            HSB _head = IterableExtensions.<HSB>head(((Iterable<HSB>)Conversions.doWrapArray(_borderColors_1)));
            Color _autoDisposeColor = this._sWTExtensions.toAutoDisposeColor(_head);
            gc.setForeground(_autoDisposeColor);
            Point _topLeft_1 = this._sWTExtensions.getTopLeft(itemBounds);
            Point _translated_1 = this._sWTExtensions.getTranslated(_topLeft_1, (-1), 0);
            Point _topRight = this._sWTExtensions.getTopRight(itemBounds);
            int _tabSpacing_3 = this.settings.getTabSpacing();
            int _max_2 = Math.max(_tabSpacing_3, 0);
            int _plus_1 = (_max_2 + 1);
            Point _translated_2 = this._sWTExtensions.getTranslated(_topRight, _plus_1, 0);
            _xblockexpression_2 = this._sWTExtensions.drawLine(gc, _translated_1, _translated_2);
          }
          _xifexpression_4 = _xblockexpression_2;
        }
        _xifexpression_3 = _xifexpression_4;
      }
      _xblockexpression = _xifexpression_3;
    }
    return _xblockexpression;
  }
  
  protected void drawTabItemBorder(final int part, final int state, final Rectangle bounds, final GC gc) {
    boolean _or = false;
    HSB[] _borderColorsFor = this._jTabRendererHelper.getBorderColorsFor(this.settings, state);
    boolean _equals = Objects.equal(_borderColorsFor, null);
    if (_equals) {
      _or = true;
    } else {
      int[] _borderPercentsFor = this._jTabRendererHelper.getBorderPercentsFor(this.settings, state);
      boolean _equals_1 = Objects.equal(_borderPercentsFor, null);
      _or = _equals_1;
    }
    if (_or) {
      return;
    }
    final Rectangle itemOutlineBounds = this._sWTExtensions.getResized(bounds, (-1), 0);
    final CTabItem item = this.tabFolder.getItem(part);
    int _borderWidth = this.settings.getBorderWidth();
    int _divide = (_borderWidth / 2);
    final Rectangle outlineOffset = this._sWTExtensions.shrink(itemOutlineBounds, _divide);
    Path outline = null;
    boolean _onBottom = this._jTabRendererHelper.getOnBottom(this.tabFolder);
    if (_onBottom) {
      throw new UnsupportedOperationException();
    }
    final Procedure1<Path> _function = new Procedure1<Path>() {
      public void apply(final Path it) {
        Rectangle _bounds = item.getBounds();
        Point _bottom = JeeeyulsTabRenderer.this._sWTExtensions.getBottom(_bounds);
        int keyLineY = (_bottom.y - 1);
        int _borderRadius = JeeeyulsTabRenderer.this.settings.getBorderRadius();
        boolean _greaterThan = (_borderRadius > 0);
        if (_greaterThan) {
          Point _topLeft = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(outlineOffset);
          int _borderRadius_1 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
          int _multiply = (_borderRadius_1 * 2);
          int _borderRadius_2 = JeeeyulsTabRenderer.this.settings.getBorderRadius();
          int _multiply_1 = (_borderRadius_2 * 2);
          Point _point = new Point(_multiply, _multiply_1);
          Rectangle corner = JeeeyulsTabRenderer.this._sWTExtensions.newRectangle(_topLeft, _point);
          JeeeyulsTabRenderer.this._sWTExtensions.relocateTopRightWith(corner, outlineOffset);
          boolean _hasFlags = JeeeyulsTabRenderer.this._sWTExtensions.hasFlags(state, SWT.SELECTED);
          if (_hasFlags) {
            int _borderWidth = JeeeyulsTabRenderer.this.settings.getBorderWidth();
            int _minus = ((JeeeyulsTabRenderer.this.tabFolder.getSize().x - JeeeyulsTabRenderer.this.settings.getMargins().width) - _borderWidth);
            it.moveTo(_minus, keyLineY);
            Point _bottomRight = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(itemOutlineBounds);
            it.lineTo(_bottomRight.x, keyLineY);
          } else {
            Point _bottomRight_1 = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(outlineOffset);
            it.moveTo(_bottomRight_1.x, keyLineY);
          }
          Point _right = JeeeyulsTabRenderer.this._sWTExtensions.getRight(corner);
          JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _right);
          JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, corner, 0, 90);
          JeeeyulsTabRenderer.this._sWTExtensions.relocateTopLeftWith(corner, outlineOffset);
          Point _top = JeeeyulsTabRenderer.this._sWTExtensions.getTop(corner);
          JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _top);
          boolean _or = false;
          HSB[] _borderColors = JeeeyulsTabRenderer.this.settings.getBorderColors();
          boolean _equals = Objects.equal(_borderColors, null);
          if (_equals) {
            _or = true;
          } else {
            boolean _and = false;
            HSB[] _borderColors_1 = JeeeyulsTabRenderer.this.settings.getBorderColors();
            boolean _notEquals = (!Objects.equal(_borderColors_1, null));
            if (!_notEquals) {
              _and = false;
            } else {
              CTabItem _firstVisibleItem = JeeeyulsTabRenderer.this._jTabRendererHelper.getFirstVisibleItem(JeeeyulsTabRenderer.this.parent);
              boolean _notEquals_1 = (!Objects.equal(item, _firstVisibleItem));
              _and = _notEquals_1;
            }
            _or = _and;
          }
          if (_or) {
            JeeeyulsTabRenderer.this._sWTExtensions.addArc(it, corner, 90, 90);
            it.lineTo(outlineOffset.x, keyLineY);
            boolean _hasFlags_1 = JeeeyulsTabRenderer.this._sWTExtensions.hasFlags(state, SWT.SELECTED);
            if (_hasFlags_1) {
              Rectangle _margins = JeeeyulsTabRenderer.this.settings.getMargins();
              Point left = new Point(_margins.x, keyLineY);
              HSB[] _borderColors_2 = JeeeyulsTabRenderer.this.settings.getBorderColors();
              boolean _notEquals_2 = (!Objects.equal(_borderColors_2, null));
              if (_notEquals_2) {
                JeeeyulsTabRenderer.this._sWTExtensions.translate(left, 1, 0);
              }
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, left);
            }
          }
        } else {
          boolean _hasFlags_2 = JeeeyulsTabRenderer.this._sWTExtensions.hasFlags(state, SWT.SELECTED);
          if (_hasFlags_2) {
            int _borderWidth_1 = JeeeyulsTabRenderer.this.settings.getBorderWidth();
            int _minus_1 = ((JeeeyulsTabRenderer.this.tabFolder.getSize().x - JeeeyulsTabRenderer.this.settings.getMargins().width) - _borderWidth_1);
            it.moveTo(_minus_1, keyLineY);
            Point _bottomRight_2 = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(itemOutlineBounds);
            it.lineTo(_bottomRight_2.x, keyLineY);
          } else {
            Point _bottomRight_3 = JeeeyulsTabRenderer.this._sWTExtensions.getBottomRight(outlineOffset);
            it.moveTo(_bottomRight_3.x, keyLineY);
          }
          Point _topRight = JeeeyulsTabRenderer.this._sWTExtensions.getTopRight(itemOutlineBounds);
          JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _topRight);
          Point _topLeft_1 = JeeeyulsTabRenderer.this._sWTExtensions.getTopLeft(itemOutlineBounds);
          JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, _topLeft_1);
          boolean _or_1 = false;
          HSB[] _borderColors_3 = JeeeyulsTabRenderer.this.settings.getBorderColors();
          boolean _equals_1 = Objects.equal(_borderColors_3, null);
          if (_equals_1) {
            _or_1 = true;
          } else {
            boolean _and_1 = false;
            HSB[] _borderColors_4 = JeeeyulsTabRenderer.this.settings.getBorderColors();
            boolean _notEquals_3 = (!Objects.equal(_borderColors_4, null));
            if (!_notEquals_3) {
              _and_1 = false;
            } else {
              CTabItem _firstVisibleItem_1 = JeeeyulsTabRenderer.this._jTabRendererHelper.getFirstVisibleItem(JeeeyulsTabRenderer.this.parent);
              boolean _notEquals_4 = (!Objects.equal(item, _firstVisibleItem_1));
              _and_1 = _notEquals_4;
            }
            _or_1 = _and_1;
          }
          if (_or_1) {
            Point _bottomLeft = JeeeyulsTabRenderer.this._sWTExtensions.getBottomLeft(itemOutlineBounds);
            it.lineTo(_bottomLeft.x, keyLineY);
            boolean _hasFlags_3 = JeeeyulsTabRenderer.this._sWTExtensions.hasFlags(state, SWT.SELECTED);
            if (_hasFlags_3) {
              Rectangle _margins_1 = JeeeyulsTabRenderer.this.settings.getMargins();
              Point left_1 = new Point(_margins_1.x, keyLineY);
              HSB[] _borderColors_5 = JeeeyulsTabRenderer.this.settings.getBorderColors();
              boolean _notEquals_5 = (!Objects.equal(_borderColors_5, null));
              if (_notEquals_5) {
                int _borderWidth_2 = JeeeyulsTabRenderer.this.settings.getBorderWidth();
                JeeeyulsTabRenderer.this._sWTExtensions.translate(left_1, _borderWidth_2, 0);
              }
              JeeeyulsTabRenderer.this._sWTExtensions.lineTo(it, left_1);
            }
          }
        }
      }
    };
    Path _newTemporaryPath = this._sWTExtensions.newTemporaryPath(_function);
    outline = _newTemporaryPath;
    int _borderWidth_1 = this.settings.getBorderWidth();
    gc.setLineWidth(_borderWidth_1);
    HSB[] _borderColorsFor_1 = this._jTabRendererHelper.getBorderColorsFor(this.settings, state);
    Color[] _autoReleaseColor = this._sWTExtensions.toAutoReleaseColor(_borderColorsFor_1);
    int[] _borderPercentsFor_1 = this._jTabRendererHelper.getBorderPercentsFor(this.settings, state);
    this._sWTExtensions.drawGradientPath(gc, outline, _autoReleaseColor, _borderPercentsFor_1, true);
  }
  
  protected GC drawTabItemBackground(final int part, final int state, final Rectangle bounds, final GC gc) {
    GC _xblockexpression = null;
    {
      final Rectangle itemBounds = this._sWTExtensions.getCopy(bounds);
      HSB[] _borderColorsFor = this._jTabRendererHelper.getBorderColorsFor(this.settings, state);
      boolean _notEquals = (!Objects.equal(_borderColorsFor, null));
      if (_notEquals) {
        this._sWTExtensions.shrink(itemBounds, 1, 0);
      }
      HSB[] colors = this._jTabRendererHelper.getItemFillFor(this.settings, state);
      GC _xifexpression = null;
      boolean _notEquals_1 = (!Objects.equal(colors, null));
      if (_notEquals_1) {
        int _borderRadius = this.settings.getBorderRadius();
        int[] _itemFillPercentsFor = this._jTabRendererHelper.getItemFillPercentsFor(this.settings, state);
        _xifexpression = this._sWTExtensions.fillGradientRoundRectangle(gc, itemBounds, _borderRadius, this._sWTExtensions.CORNER_TOP, colors, _itemFillPercentsFor, true);
      }
      _xblockexpression = _xifexpression;
    }
    return _xblockexpression;
  }
  
  protected GC drawShadow(final int part, final int state, final Rectangle bounds, final GC gc) {
    final Procedure1<GC> _function = new Procedure1<GC>() {
      public void apply(final GC it) {
        NinePatch _shadow = JeeeyulsTabRenderer.this.getShadow();
        Rectangle _tabArea = JeeeyulsTabRenderer.this.tabArea();
        int _shadowRadius = JeeeyulsTabRenderer.this.settings.getShadowRadius();
        Rectangle _expanded = JeeeyulsTabRenderer.this._sWTExtensions.getExpanded(_tabArea, _shadowRadius);
        Point _shadowPosition = JeeeyulsTabRenderer.this.settings.getShadowPosition();
        Rectangle _translate = JeeeyulsTabRenderer.this._sWTExtensions.translate(_expanded, _shadowPosition);
        _shadow.fill(gc, _translate);
      }
    };
    return this._sWTExtensions.withClip(gc, bounds, _function);
  }
  
  protected Rectangle tabArea() {
    Rectangle _newRectangle = this._sWTExtensions.newRectangle();
    Point _size = this.tabFolder.getSize();
    Rectangle _setSize = this._sWTExtensions.setSize(_newRectangle, _size);
    Rectangle _margins = this.settings.getMargins();
    Rectangle _margins_1 = this.settings.getMargins();
    Rectangle _margins_2 = this.settings.getMargins();
    return this._sWTExtensions.shrink(_setSize, _margins.x, 0, _margins_1.width, _margins_2.height);
  }
  
  protected NinePatch getShadow() {
    boolean _or = false;
    boolean _equals = Objects.equal(this.shadowNinePatch, null);
    if (_equals) {
      _or = true;
    } else {
      boolean _isDisposed = this.shadowNinePatch.isDisposed();
      _or = _isDisposed;
    }
    if (_or) {
      HSB _shadowColor = this.settings.getShadowColor();
      RGB _rGB = _shadowColor.toRGB();
      int _borderRadius = this.settings.getBorderRadius();
      int _plus = (_borderRadius + 3);
      int _shadowRadius = this.settings.getShadowRadius();
      NinePatch _createShadowPatch = Shadow9PatchFactory.createShadowPatch(_rGB, _plus, _shadowRadius);
      this.shadowNinePatch = _createShadowPatch;
    }
    return this.shadowNinePatch;
  }
  
  public JTabSettings getSettings() {
    return this.settings;
  }
  
  public CTabFolder getTabFolder() {
    return this.tabFolder;
  }
}
