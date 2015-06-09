/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie;

import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import sune.apps.ie.coloradjustment.ColorAdjustmentBrightness;
import sune.apps.ie.coloradjustment.ColorAdjustmentContrast;
import sune.apps.ie.coloradjustment.ColorAdjustmentGammaCorrection;
import sune.apps.ie.coloradjustment.ColorAdjustmentGrayscale;
import sune.apps.ie.coloradjustment.ColorAdjustmentHue;
import sune.apps.ie.coloradjustment.ColorAdjustmentInvert;
import sune.apps.ie.coloradjustment.ColorAdjustmentLightness;
import sune.apps.ie.coloradjustment.ColorAdjustmentSaturation;
import sune.apps.ie.coloradjustment.ColorAdjustmentTransparency;
import sune.apps.ie.component.ColorPalettePanel;
import sune.apps.ie.component.DraggableVBox;
import sune.apps.ie.component.IScrollPane;
import sune.apps.ie.filter.FilterBoxBlur;
import sune.apps.ie.filter.FilterCustomFilter;
import sune.apps.ie.filter.FilterEdgeDetection;
import sune.apps.ie.filter.FilterGaussianBlur;
import sune.apps.ie.filter.FilterMotionBlur;
import sune.apps.ie.filter.FilterPressUp;
import sune.apps.ie.filter.FilterPressUpAndSmoothing;
import sune.apps.ie.filter.FilterSharpen;
import sune.apps.ie.filter.FilterSmoothing;
import sune.apps.ie.filter.FilterUnsharpMask;
import sune.apps.ie.graphics.ImageUtils;
import sune.apps.ie.graphics.LayerBlendMode;
import sune.apps.ie.layer.BackgroundLayer;
import sune.apps.ie.layer.EmptyLayer;
import sune.apps.ie.layer.ImageLayer;
import sune.apps.ie.layer.Layer;
import sune.apps.ie.layer.LayerMask;
import sune.apps.ie.layer.LayersManager;
import sune.apps.ie.plugin.Plugin;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.IconRegistry;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.registry.ProgressBackups;
import sune.apps.ie.registry.ProgressBackups.ProgressBackup;
import sune.apps.ie.registry.Registry;
import sune.apps.ie.registry.ResourceLoader;
import sune.apps.ie.registry.ThreadRegistry;
import sune.apps.ie.registry.ToolsRegistry;
import sune.apps.ie.selection.Selection;
import sune.apps.ie.tool.MaskTool;
import sune.apps.ie.tool.Tool;
import sune.apps.ie.tool.ToolBrush;
import sune.apps.ie.tool.ToolMove;
import sune.apps.ie.tool.ToolPencil;
import sune.apps.ie.tool.ToolResize;
import sune.apps.ie.tool.ToolRotate;
import sune.apps.ie.tool.ToolRubber;
import sune.apps.ie.tool.ToolSelection;
import sune.apps.ie.tool.ToolViewMove;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.util.ByteHelper;
import sune.apps.ie.util.FileSave;
import sune.apps.ie.util.Utils;
import sune.utils.ssdf.SSDArray;
import sune.utils.ssdf.SSDFCore;
import sune.utils.ssdf.SSDObject;

public class Editor extends Window
{
	private BorderPane paneMain;
	private CanvasPanel canvas;
	private IScrollPane scrollPane;

	private StackPane paneBottom;
	private VBox paneTop;
	private VBox paneTool;
	private Label lblStatus;
	private Label lblToolInfo;

	private VBox paneRight;
	private ScrollPane paneLayers;
	private DraggableVBox paneLayersList;
	
	private HBox paneLayerButtons;
	private Button btnAddLayer;
	
	private VBox paneLeft;
	private VBox paneTools;
	
	private MenuBar mbarMain;
	private Menu menuFile;
	private Menu menuEdits;
	private Menu menuImage;
	private Menu menuFilters;
	private Menu menuSelection;
	
	private MenuItem menuItemNew;
	private MenuItem menuItemOpen;
	private MenuItem menuItemSave;
	private MenuItem menuItemSaveAs;
	private MenuItem menuItemExit;
	
	private MenuItem menuItemBackward;
	private MenuItem menuItemForward;
	private MenuItem menuItemConfig;
	
	private MenuItem menuItemResize;
	private MenuItem menuItemFlipImageH;
	private MenuItem menuItemFlipImageV;
	private MenuItem menuItemFlipImageLeft;
	private MenuItem menuItemFlipImageRight;
	
	private MenuItem menuItemSelDeselect;
	private MenuItem menuItemSelSelectAll;
	private MenuItem menuItemSelInvert;

	private ColorPalettePanel colorPalettePanel;
	private ContextMenu contextMenu;
	
	private List<EventHandler<KeyEvent>> keyListeners = new ArrayList<>();
	private List<Runnable> runs = new ArrayList<>();
	
	private int previewFactor = 30;
	private int previewWidth  = 30;
	private int previewHeight = 30;
	
	private File openedFile;
	private String openedFileExt;
	
	private HBox boxBlendModes;
	private Label lblBlendModes;
	private ComboBox<LayerBlendMode> cmbBlendModes;
	
	private List<File> openQueue = new ArrayList<>();
	private List<FileSave> saveQueue = new ArrayList<>();
	private int openAttempt;
	private int saveAttempt;
	
	private boolean initialized;
	
	public Editor()
	{
		super(new Stage());
		
		this.stage.setTitle(Translation.getTranslation("APP_TITLE", Main.VERSION));
		this.stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				for(EventHandler<KeyEvent> handler : keyListeners)
					handler.handle(event);
			}
		});
		
		this.paneMain = new BorderPane();

		this.scrollPane = new IScrollPane(this);
		this.canvas 	= new CanvasPanel(this);

		this.scrollPane.setId("mainScrollPane");
		this.scrollPane.setContent(canvas);
		
		this.scrollPane.setBorder(null);
		this.scrollPane.setPadding(new Insets(0));
		
		this.paneTop = new VBox();
		this.paneTop.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1), new Insets(-1, -1, 0, -1))));

		this.paneBottom = new StackPane();
		this.paneBottom.setPadding(new Insets(5));
		this.paneBottom.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1), new Insets(0, -1, -1, -1))));

		this.paneLeft = new VBox();
		this.paneLeft.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1), new Insets(-1, 0, -1, -1))));
		this.paneLeft.setPadding(new Insets(10));
		
		this.paneRight = new VBox();
		this.paneRight.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1), new Insets(-1, -1, -1, 0))));
		
		this.paneTool = new VBox();
		
		ToolsRegistry.registerTool(new ToolBrush(this, canvas));
		ToolsRegistry.registerTool(new ToolPencil(this, canvas));
		ToolsRegistry.registerTool(new ToolRubber(this, canvas));
		ToolsRegistry.registerTool(new ToolMove(this, canvas));
		ToolsRegistry.registerTool(new ToolRotate(this, canvas));
		ToolsRegistry.registerTool(new ToolResize(this, canvas));
		ToolsRegistry.registerTool(new ToolSelection(this, canvas));
		ToolsRegistry.registerTool(new ToolViewMove(this, canvas));
		
		this.canvas.setTool(ToolsRegistry.getToolByName(ToolPencil.NAME));
		reloadToolPanel();
		
		this.paneTools = new VBox(5);
		this.paneLeft.getChildren().add(paneTools);
		reloadTools();
		
		this.colorPalettePanel = new ColorPalettePanel(this);
		this.paneRight.getChildren().add(colorPalettePanel);
		
		this.boxBlendModes = new HBox(5);
		this.boxBlendModes.setPadding(new Insets(10, 10, 5, 10));
		this.boxBlendModes.setAlignment(Pos.CENTER_LEFT);
		
		this.lblBlendModes = new Label(Translation.getTranslation("general.texts.layerBlendMode"));
		this.boxBlendModes.getChildren().add(lblBlendModes);
		
		Pane pBM = new Pane();
		this.boxBlendModes.getChildren().add(pBM);
		HBox.setHgrow(pBM, Priority.ALWAYS);
		
		this.cmbBlendModes = new ComboBox<LayerBlendMode>();
		this.cmbBlendModes.getItems().addAll(LayerBlendMode.values());
		this.cmbBlendModes.getSelectionModel().select(0);
		this.boxBlendModes.getChildren().add(cmbBlendModes);
		
		this.cmbBlendModes.valueProperty().addListener(new ChangeListener<LayerBlendMode>()
		{
			@Override
			public void changed(ObservableValue<? extends LayerBlendMode> observable, LayerBlendMode oldValue, LayerBlendMode newValue)
			{
				canvas.getLayersManager().getSelectedLayer().setBlendMode(newValue);
				canvas.redraw();
			}
		});
		
		this.paneLayers = new ScrollPane();
		this.paneLayers.setMinWidth(200);
		
		this.paneLayersList = new DraggableVBox(this);
		this.paneLayers.setContent(paneLayersList);

		VBox.setMargin(paneLayers, new Insets(5));
		VBox.setVgrow(paneLayers, Priority.ALWAYS);
		this.paneLayers.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
			CornerRadii.EMPTY, new BorderWidths(1))));
		this.paneLayersList.setBackground(new Background(
			new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, new Insets(0))));
		this.paneLayersList.setPadding(new Insets(0));
		this.paneLayersList.setPrefWidth(200);
		
		this.paneLayerButtons = new HBox(5);
		this.paneLayerButtons.setPadding(new Insets(0, 5, 5, 5));
		this.paneLayerButtons.setAlignment(Pos.CENTER_RIGHT);
		
		this.btnAddLayer = new Button(Translation.getTranslation("buttons.addLayer"));
		this.btnAddLayer.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(!canvas.isInitialized())
					return;
				
				EmptyLayer emptyLayer = new EmptyLayer(
					canvas.leftOffset(), canvas.topOffset(),
					canvas.getCanvasWidth(), canvas.getCanvasHeight()
				);
				
				canvas.getLayersManager().addLayer(emptyLayer);
				paneLayersList.getSelection().clear();
				paneLayersList.getSelection().add(0);
				canvas.redraw();
				
				reloadLayers();
				canvas.getTool().reset();
			}
		});
		
		this.lblStatus = new Label(Translation.getTranslation("messages.startup.welcomeMessage", Main.VERSION));
		this.lblStatus.setAlignment(Pos.CENTER);
		StackPane.setAlignment(lblStatus, Pos.CENTER_LEFT);
		
		this.lblToolInfo = new Label();
		this.lblToolInfo.setAlignment(Pos.CENTER);
		StackPane.setAlignment(lblToolInfo, Pos.CENTER_RIGHT);

		this.mbarMain 	 	= new MenuBar();
		this.menuFile 	 	= new Menu(Translation.getTranslation("menus.file.MENU_TITLE"));
		this.menuEdits 	 	= new Menu(Translation.getTranslation("menus.edits.MENU_TITLE"));
		this.menuImage 	 	= new Menu(Translation.getTranslation("menus.image.MENU_TITLE"));
		this.menuFilters 	= new Menu(Translation.getTranslation("menus.filters.MENU_TITLE"));
		this.menuSelection 	= new Menu(Translation.getTranslation("menus.selection.MENU_TITLE"));
		
		this.menuItemNew = new MenuItem(Translation.getTranslation("menus.file.new"));
		this.menuItemNew.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				createNewImagePrompt();
			}
		});
		
		this.menuItemOpen = new MenuItem(Translation.getTranslation("menus.file.open"));
		this.menuItemOpen.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				openPrompt();
			}
		});
		
		this.menuItemSave = new MenuItem(Translation.getTranslation("menus.file.save"));
		this.menuItemSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				if(openedFile != null && !openedFileExt.isEmpty())	addToSaveQueue(openedFile, openedFileExt);
				else 												saveAsPrompt();
			}
		});
		
		this.menuItemSaveAs = new MenuItem(Translation.getTranslation("menus.file.saveAs"));
		this.menuItemSaveAs.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				saveAsPrompt();
			}
		});
		
		this.menuItemExit = new MenuItem(Translation.getTranslation("menus.file.exit"));
		this.menuItemExit.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				close();
			}
		});

		this.menuFile.getItems().add(menuItemNew);
		this.menuFile.getItems().add(menuItemOpen);
		this.menuFile.getItems().add(menuItemSave);
		this.menuFile.getItems().add(menuItemSaveAs);
		this.menuFile.getItems().add(menuItemExit);
		
		this.menuItemBackward = new MenuItem(Translation.getTranslation("menus.edits.backward"));
		this.menuItemBackward.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				stepBackward();
			}
		});
		
		this.menuItemForward = new MenuItem(Translation.getTranslation("menus.edits.forward"));
		this.menuItemForward.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				stepForward();
			}
		});
		
		this.menuItemConfig = new MenuItem(Translation.getTranslation("menus.edits.config"));
		this.menuItemConfig.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				new WindowConfiguration(Editor.this).show();
			}
		});
		
		this.menuEdits.setOnShowing(new EventHandler<Event>()
		{
			@Override
			public void handle(Event event)
			{
				menuItemBackward.setDisable(!ProgressBackups.hasPreviousBackup());
				menuItemForward.setDisable(!ProgressBackups.hasNextBackup());
			}
		});
		
		this.menuEdits.getItems().add(menuItemBackward);
		this.menuEdits.getItems().add(menuItemForward);
		this.menuEdits.getItems().add(new SeparatorMenuItem());
		this.menuEdits.getItems().add(menuItemConfig);
		
		this.menuItemResize = new MenuItem(Translation.getTranslation("menus.image.resize"));
		this.menuItemResize.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				new WindowResize(Editor.this).show();
			}
		});
		
		this.menuItemFlipImageH = new MenuItem(Translation.getTranslation("menus.image.flipImageH"));
		this.menuItemFlipImageH.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Layer layer;
				if((layer = canvas.getLayersManager().getSelectedLayer()) instanceof ImageLayer)
					((ImageLayer) layer).flipHorizontally();
				
				reloadLayers();
				canvas.redraw();
			}
		});
		
		this.menuItemFlipImageV = new MenuItem(Translation.getTranslation("menus.image.flipImageV"));
		this.menuItemFlipImageV.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Layer layer;
				if((layer = canvas.getLayersManager().getSelectedLayer()) instanceof ImageLayer)
					((ImageLayer) layer).flipVertically();
				
				reloadLayers();
				canvas.redraw();
			}
		});
		
		this.menuItemFlipImageLeft = new MenuItem(Translation.getTranslation("menus.image.flipImageLeft"));
		this.menuItemFlipImageLeft.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Layer layer;
				if((layer = canvas.getLayersManager().getSelectedLayer()) instanceof ImageLayer)
					((ImageLayer) layer).flipToLeft();
				
				reloadLayers();
				canvas.redraw();
			}
		});
		
		this.menuItemFlipImageRight = new MenuItem(Translation.getTranslation("menus.image.flipImageRight"));
		this.menuItemFlipImageRight.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Layer layer;
				if((layer = canvas.getLayersManager().getSelectedLayer()) instanceof ImageLayer)
					((ImageLayer) layer).flipToRight();
				
				reloadLayers();
				canvas.redraw();
			}
		});
		
		this.menuImage.getItems().add(menuItemResize);
		this.menuImage.getItems().add(menuItemFlipImageH);
		this.menuImage.getItems().add(menuItemFlipImageV);
		this.menuImage.getItems().add(menuItemFlipImageLeft);
		this.menuImage.getItems().add(menuItemFlipImageRight);
		this.menuImage.getItems().add(new SeparatorMenuItem());
		
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentContrast(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentBrightness(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentInvert(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentGrayscale(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentHue(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentGammaCorrection(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentLightness(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentTransparency(this));
		Registry.COLOR_ADJUSTMENTS.register(new ColorAdjustmentSaturation(this));
		
		for(Plugin plugin : Registry.COLOR_ADJUSTMENTS.getAll())
		{
			String pluginName 		= plugin.getName();
			MenuItem menuItemPlugin = new MenuItem(Translation.getTranslation("plugins." + pluginName + ".title"));
			
			menuItemPlugin.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					plugin.load();
				}
			});
			
			menuImage.getItems().add(menuItemPlugin);
		}
		
		Registry.FILTERS.register(new FilterGaussianBlur(this));
		Registry.FILTERS.register(new FilterBoxBlur(this));
		Registry.FILTERS.register(new FilterMotionBlur(this));
		Registry.FILTERS.register(new FilterSharpen(this));
		Registry.FILTERS.register(new FilterEdgeDetection(this));
		Registry.FILTERS.register(new FilterSmoothing(this));
		Registry.FILTERS.register(new FilterUnsharpMask(this));
		Registry.FILTERS.register(new FilterPressUp(this));
		Registry.FILTERS.register(new FilterPressUpAndSmoothing(this));
		Registry.FILTERS.register(new FilterCustomFilter(this));
		
		for(Plugin plugin : Registry.FILTERS.getAll())
		{
			String pluginName 		= plugin.getName();
			MenuItem menuItemPlugin = new MenuItem(Translation.getTranslation("plugins." + pluginName + ".title"));
			
			menuItemPlugin.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					plugin.load();
				}
			});
			
			menuFilters.getItems().add(menuItemPlugin);
		}
		
		this.menuItemSelDeselect = new MenuItem(Translation.getTranslation("menus.selection.deselect"));
		this.menuItemSelDeselect.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				canvas.deselectLayersSelections();
				
				canvas.redraw();
				reloadLayers();
			}
		});
		
		this.menuItemSelSelectAll = new MenuItem(Translation.getTranslation("menus.selection.selectAll"));
		this.menuItemSelSelectAll.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				canvas.deselectLayersSelections();
				
				Layer selectedLayer = canvas.getLayersManager().getSelectedLayer();
				Selection selection = selectedLayer.createSelection();
				selection.addRegion(0, 0, (int) selectedLayer.getWidth(1), (int) selectedLayer.getHeight(1));
				selection.prepare();
				
				canvas.redraw();
				reloadLayers();
			}
		});
		
		this.menuItemSelInvert = new MenuItem(Translation.getTranslation("menus.selection.invert"));
		this.menuItemSelInvert.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Layer selectedLayer = canvas.getLayersManager().getSelectedLayer();
				Selection selection = selectedLayer.getSelection();
				selection.invert();
				selection.prepare();
				canvas.redraw();
				reloadLayers();
			}
		});
		
		this.menuSelection.getItems().add(menuItemSelDeselect);
		this.menuSelection.getItems().add(menuItemSelSelectAll);
		this.menuSelection.getItems().add(menuItemSelInvert);
		
		this.mbarMain.getMenus().add(menuFile);
		this.mbarMain.getMenus().add(menuEdits);
		this.mbarMain.getMenus().add(menuImage);
		this.mbarMain.getMenus().add(menuFilters);
		this.mbarMain.getMenus().add(menuSelection);
		this.paneTop.getChildren().add(mbarMain);

		this.menuItemSave.setDisable(true);
		this.menuItemSaveAs.setDisable(true);
		this.menuImage.setDisable(true);
		this.menuFilters.setDisable(true);
		this.menuSelection.setDisable(true);
		this.btnAddLayer.setDisable(true);
		this.cmbBlendModes.setDisable(true);
		
		// KEY SHORTCUTS
		this.scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if(event.isControlDown())
				{
					switch(event.getCode())
					{
						case N:
							createNewImagePrompt();
							break;
						case O:
							openPrompt();
							break;
						case S:
							if(openedFile != null && !openedFileExt.isEmpty() &&
							   !event.isShiftDown())	addToSaveQueue(openedFile, openedFileExt);
							else 						saveAsPrompt();
							break;
						case D:
							canvas.deselectLayersSelections();
							canvas.setTToolsVisible(true);
							canvas.redraw();
							break;
						case SPACE:
							scrollPane.scrollToCenter();
							break;
						case A:
							canvas.deselectLayersSelections();
							
							Layer layer = canvas.getLayersManager().getSelectedLayer();
							Selection selection = layer.createSelection();
							selection.addRegion(
								0, 0, (int) layer.getWidth(1)-1, (int) layer.getHeight(1)-1);
							selection.prepare();
							canvas.redraw();
							break;
						case Z:
							stepBackward();
							break;
						case Y:
							stepForward();
							break;
						default:
							break;
					}
				}
			}
		});
		
		this.paneBottom.getChildren().add(lblStatus);
		this.paneBottom.getChildren().add(lblToolInfo);
		
		this.paneRight.getChildren().add(boxBlendModes);
		this.paneRight.getChildren().add(paneLayers);
		this.paneLayerButtons.getChildren().add(btnAddLayer);
		this.paneRight.getChildren().add(paneLayerButtons);
		
		this.paneMain.setCenter(scrollPane);
		this.paneMain.setBottom(paneBottom);
		this.paneMain.setTop(paneTop);
		this.paneMain.setLeft(paneLeft);
		this.paneMain.setRight(paneRight);

		this.paneTop.getChildren().add(paneTool);
		
		this.scene = new Scene(paneMain, 800, 500);
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.stage.setScene(scene);
		
		this.stage.setResizable(true);
		this.stage.setMaximized(true);
		this.stage.centerOnScreen();

		this.stage.getIcons().add(Utils.APP_ICON);
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>()
		{
			@Override
			public void handle(WindowEvent event)
			{
				close();
			}
		});
		
		InvalidationListener listener = new InvalidationListener()
		{
		    @Override
		    public void invalidated(Observable o)
		    {
		    	if(canvas.isInitialized())
		    		resizeCanvas();
		    	else
		    	{
		    		canvas.setWidth(scrollPane.getWidth());
		    		canvas.setHeight(scrollPane.getHeight());
		    		canvas.redraw();
		    	}
		    }
		};
		
		ThreadRegistry.registerThreadAndStart(threadOpen);
		ThreadRegistry.registerThreadAndStart(threadSave);
		
		this.scrollPane.widthProperty().addListener(listener);
		this.scrollPane.heightProperty().addListener(listener);
		this.scrollPane.requestFocus();
		
		// "sw" or "j2d" for software and "d3d" or "es2" for hardware accelerated
		//System.out.println(com.sun.prism.GraphicsPipeline.getPipeline().getClass().getName());
	}
	
	public void resetOpenedFile()
	{
		openedFile 		= null;
		openedFileExt 	= null;
	}
	
	public void createNewImagePrompt()
	{
		new WindowNew(this).show();
	}
	
	public void openPrompt()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Translation.getTranslation("menus.file.open"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.all"), "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.jpg"), "*.jpg", "*.jpeg"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.png"), "*.png"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.gif"), "*.gif"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.bmp"), "*.bmp"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.sie"), "*.sie"));
		
		if(openedFile != null)
		fileChooser.setInitialDirectory(openedFile.getParentFile());
		
		File selectedFile;
		if((selectedFile = fileChooser.showOpenDialog(stage)) != null)
		{
			canvas.clear();
			addFile(selectedFile);
		}
	}
	
	public void saveAsPrompt()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(Translation.getTranslation("menus.file.saveAs"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.jpg"), "*.jpg", "*.jpeg"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.png"), "*.png"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.gif"), "*.gif"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.bmp"), "*.bmp"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter(
			Translation.getTranslation("misc.fileSelection.sie"), "*.sie"));
		
		if(openedFile != null)
		fileChooser.setInitialDirectory(openedFile.getParentFile());
		
		File selectedFile;
		if((selectedFile = fileChooser.showSaveDialog(stage)) != null)
		{
			// Gets the first extension from the list (*.jpg, *.jpeg; will be *.jpg)
			String ext = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
			// Formats the extension (*.jpg -> jpg)
			ext = ext.substring(2).trim();
			
			addToSaveQueue(selectedFile, ext);
			if(openedFile == null && openedFileExt == null)
			{
				openedFile 		= selectedFile;
				openedFileExt 	= ext;
			}
		}
	}
	
	public void canvasInit()
	{
		menuItemSave.setDisable(false);
		menuItemSaveAs.setDisable(false);
		menuImage.setDisable(false);
		menuFilters.setDisable(false);
		menuSelection.setDisable(false);
		btnAddLayer.setDisable(false);
		cmbBlendModes.setDisable(false);
	}
	
	@Override
	public void show()
	{
		super.show();
		
		canvas.setSize(scrollPane.getWidth(), scrollPane.getHeight());
		canvas.redraw();
	}
	
	public void init()
	{
		for(Runnable runnable : runs)
			runnable.run();
		
		initialized = true;
	}
	
	public Dimension2D getCanvasDimensions()
	{
		Insets insets = scrollPane.getInsets();

    	double paneWidth  = scrollPane.getWidth() - insets.getLeft() - insets.getRight();
    	double paneHeight = scrollPane.getHeight() - insets.getTop() - insets.getBottom();
    	
		Insets padding = canvas.getPadding();
		double left    = padding.getLeft();
		double right   = padding.getRight();
		double top 	   = padding.getTop();
		double bottom  = padding.getBottom();
		
		double canvasWidth  = Math.max(canvas.getCanvasWidth() + left + right, paneWidth);
		double canvasHeight = Math.max(canvas.getCanvasHeight() + top + bottom, paneHeight);
		
		return new Dimension2D(canvasWidth, canvasHeight);
	}
	
	public void resizeCanvas()
	{
		Dimension2D dims = getCanvasDimensions();
    	canvas.reload(dims.getWidth(), dims.getHeight());
        canvas.redraw();
        
        if(!initialized)
        	init();
	}
	
	public void close()
	{
		canvas.getTool().unload();
		ThreadRegistry.closeAllThreads();
		Platform.exit();
	}

	public void afterInit(Runnable runnable)
	{
		runs.add(runnable);
	}
	
	private Thread threadSave = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			while(ThreadRegistry.isRunning)
			{
				for(int i = 0; i < saveQueue.size(); i++)
				{
					if(saveFile(saveQueue.get(i)) || saveAttempt >= 5)
					{
						saveQueue.remove(i--);
						saveAttempt = 0;
					}
				}
				
				Utils.sleep(1);
			}
		}
	});

	private Thread threadOpen = new Thread(new Runnable()
	{
		@Override
		public void run()
		{
			while(ThreadRegistry.isRunning)
			{
				for(int i = 0; i < openQueue.size(); i++)
				{
					if(openFile(openQueue.get(i)) || openAttempt >= 5)
					{
						openQueue.remove(i--);
						openAttempt = 0;
						
						ProgressBackups.createBackup(Editor.this);
					}
				}
				
				Utils.sleep(1);
			}
		}		
	});
	
	public void addFile(File file)
	{
		openQueue.add(file);
	}
	
	public boolean openFile(File file)
	{
		try
		{
			String filePath = file.getAbsolutePath();
			setStatus(Translation.getTranslation("general.texts.fileOpening", filePath));
			
			String[] splitPath 	= filePath.split("\\\\");
			String fileName 	= splitPath[splitPath.length-1];
			String[] splitName 	= fileName.split("\\.");
			String extension 	= splitName[splitName.length-1].trim();
			
			boolean empty = canvas.getLayersManager().isEmpty();
			boolean isSIE = extension.equalsIgnoreCase("sie");
			
			if(isSIE)
			{
				ProgressBackup backup 		= ProgressBackups.loadBackup(filePath);
				List<WritableImage> images 	= backup.images;
				
				double canvasWidth  = 0;
				double canvasHeight = 0;
				
				Map<String, LayerData> layersData = getLayersData(backup.data);
				List<Layer> layersToAdd 		  = new ArrayList<>();
				
				for(Entry<String, LayerData> entry : layersData.entrySet())
				{
					try
					{
						LayerData layerData = entry.getValue();
						int index = Integer.parseInt(layerData.data.get("image").toString());
						
						double posX 	= Double.parseDouble(layerData.data.get("offsetX").toString());
						double posY 	= Double.parseDouble(layerData.data.get("offsetY").toString());
						double width 	= Double.parseDouble(layerData.data.get("width").toString());
						double height 	= Double.parseDouble(layerData.data.get("height").toString());
						
						WritableImage layerImage = images.get(index);
						ImageLayer imageLayer 	 = new ImageLayer(layerImage,
							canvas.getBackgroundLayer().getX(1) + posX,
							canvas.getBackgroundLayer().getY(1) + posY,
							width, height);
						imageLayer.setName(layerData.name);
						
						if(imageLayer.getWidth(1)  > IScrollPane.MAX_WIDTH ||
						   imageLayer.getHeight(1) > IScrollPane.MAX_HEIGHT)
							continue;
						
						if(width > canvasWidth) 	canvasWidth  = width;
						if(height > canvasHeight) 	canvasHeight = height;
						
						layersToAdd.add(imageLayer);
					}
					catch(Exception ex) {}
				}
				
				canvas.create(canvasWidth, canvasHeight, Color.TRANSPARENT, false);
				
				LayersManager layersManager = canvas.getLayersManager();
				for(Layer layer : layersToAdd) layersManager.addLayer(layer);
				paneLayersList.getSelection().clear();
				paneLayersList.getSelection().add(0);
				
				canvas.getTool().reset();
				canvas.getTool().unload();
				resizeCanvas();
				resetOpenedFile();
				
				Utils.run(() ->
				{
					String[] split 	= file.getName().split("\\.");
					openedFileExt 	= split[split.length-1].trim();
					openedFile 		= file;
					
					stage.setTitle(Translation.getTranslation("APP_TITLE_FILE", Main.VERSION, filePath));
				});
				
				scrollPane.scrollToCenter();
				setStatus("File " + filePath + " opened.");
				return true;
			}
			
			FileInputStream stream = new FileInputStream(file);
			WritableImage image = ImageUtils.toWritableImage(new Image(stream));
			
			double width  = image.getWidth();
			double height = image.getHeight();
			
			if(empty && !canvas.isInitialized())
				canvas.create(width, height, Color.TRANSPARENT, false);
			
			ImageLayer layer = new ImageLayer(image, 0, 0, width, height);
			if(layer.getWidth(1)  > IScrollPane.MAX_WIDTH ||
			   layer.getHeight(1) > IScrollPane.MAX_HEIGHT)
				return false;
			
			canvas.getLayersManager().addLayer(layer);
			paneLayersList.getSelection().clear();
			paneLayersList.getSelection().add(0);
			resizeCanvas();
			
			if(empty)
			{
				Utils.run(() ->
				{
					String[] split 	= file.getName().split("\\.");
					openedFileExt 	= split[split.length-1].trim();
					openedFile 		= file;
					
					stage.setTitle(Translation.getTranslation("APP_TITLE_FILE", Main.VERSION, filePath));
				});
			}
			else
			{
				Utils.run(() ->
				{
					canvas.centerLayer(layer);
					canvas.redraw();
					
					reloadLayers();
				});
			}
			
			Utils.run(() ->
			{
				canvas.getLayersManager().selectLastLayer();
				canvas.getTool().unload();
				canvas.getTool().reset();
				canvas.getTool().setLayer(layer);
			});
			
			scrollPane.scrollToCenter();
			setStatus(Translation.getTranslation("general.texts.fileOpened", filePath));
			return true;
		}
		catch(Exception ex)
		{
			setStatus(Translation.getTranslation("general.texts.fileOpenError", file.getAbsolutePath()));
			openAttempt++;
		}
		
		return false;
	}
	
	public void addToSaveQueue(File file, String extension)
	{
		saveQueue.add(new FileSave(file, extension));
	}
	
	public boolean saveFile(FileSave save)
	{
		try
		{
			File file 			= save.getFile();
			String extension 	= save.getExtension();
			
			String filePath = file.getAbsolutePath();
			setStatus(Translation.getTranslation("general.texts.fileSaving", filePath));
			
			if(extension.equals("sie"))
			{
				byte[] bytes 			 = buildProgressBackupFileContent();
				BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
				
				bos.write(bytes);
				bos.flush();
				bos.close();
				
				canvas.reset();
				setStatus(Translation.getTranslation("general.texts.fileSaved", filePath));
				return true;
			}
			
			WritableImage image = null;
			canvas.requestSave(
				extension.equals("jpg") || extension.equals("bmp") ? Color.WHITE :
				Color.TRANSPARENT);
			
			do
			{
				image = canvas.getImageSnapshot();
				Utils.sleep(1);
			}
			while(image == null);
			
			if(image != null)
			{
				int width  = (int) image.getWidth();
				int height = (int) image.getHeight();
				
				BufferedImage buffImage = new BufferedImage(width, height,
					extension.equals("jpg") || extension.equals("bmp") ? BufferedImage.TYPE_INT_RGB :
					BufferedImage.TYPE_INT_ARGB);
				
				Graphics2D g2d = buffImage.createGraphics();
				g2d.drawImage(SwingFXUtils.fromFXImage(image, null), 0, 0, width, height, null);
				g2d.dispose();
				
				ImageIO.write(buffImage, extension, file);
				canvas.reset();
				
				setStatus(Translation.getTranslation("general.texts.fileSaved", filePath));
				return true;
			}
		}
		catch(Exception ex)
		{
			setStatus(Translation.getTranslation("general.texts.fileSaveError", save.getFile().getAbsolutePath()));
			saveAttempt++;
		}
		
		return false;
	}
	
	public void reloadTools()
	{
		if(paneTools == null) return;
		paneTools.getChildren().clear();
		
		for(Tool tool : ToolsRegistry.getRegisteredTools())
		{
			HBox paneTool = new HBox();
			Pane paneIcon = new Pane();

			Image icon 		  = tool.getIcon();
			double iconWidth  = icon.getWidth();
			double iconHeight = icon.getHeight();
			Canvas canvasIcon = new Canvas(iconWidth, iconHeight);
			
			paneIcon.setMinSize(iconWidth, iconHeight);
			paneIcon.setPrefSize(iconWidth, iconHeight);
			
			canvasIcon.getGraphicsContext2D().drawImage(icon, 0, 0, iconWidth, iconHeight);
			paneIcon.getChildren().add(canvasIcon);
			
			paneTool.setBorder(new Border(new BorderStroke(
					canvas.getTool() == tool ? Color.BLACK : Color.TRANSPARENT,
					BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));
			
			if(canvas.getTool() == tool)
			{
				paneTool.setBackground(new Background(new BackgroundFill(
						Color.LIGHTBLUE, CornerRadii.EMPTY, new Insets(0))));
			}
			
			Tooltip toolTip = new Tooltip(tool.getTitle());
			Tooltip.install(paneTool, toolTip);
			
			paneTool.setOnMouseClicked(new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					canvas.getTool().reset();
					canvas.getTool().unload();
					
					tool.reset();
					canvas.setTool(tool);
					
					reloadToolPanel();
					reloadTools();
				}
			});
			
			paneTool.setCursor(Cursor.HAND);
			paneTool.getChildren().add(paneIcon);
			this.paneTools.getChildren().add(paneTool);
		}
	}

	public void reloadLayers()
	{
		Utils.run(() ->
		{
			reloadLayers(canvas.getLayersManager().getLayers());
		});
	}
	
	private void reloadLayers(List<Layer> layers)
	{
		if(paneLayersList == null) return;
		paneLayersList.getChildren().clear();
		paneLayersList.setMinWidth(paneLayers.getViewportBounds().getWidth());	

		LayersManager layersManager = canvas.getLayersManager();
		Layer selectedLayer 		= layersManager.getSelectedLayer();
		
		// PREVIEWS
		boolean landscape 		= true;
		double backgroundX		= 0;
		double backgroundY		= 0;
		double backgroundWidth  = 0;
		double backgroundHeight = 0;
		double backgroundRatio 	= 0;
		
		Layer backgroundLayer;
		if((backgroundLayer = canvas.getBackgroundLayer()) != null)
		{
			backgroundX		 = backgroundLayer.getX(1);
			backgroundY		 = backgroundLayer.getY(1);
			backgroundWidth	 = backgroundLayer.getWidth(1);
			backgroundHeight = backgroundLayer.getHeight(1);
			landscape  		 = backgroundWidth >= backgroundHeight;
			backgroundRatio  = landscape ? backgroundHeight / backgroundWidth :
							   backgroundWidth / backgroundHeight;
		}
		
		for(int i = layers.size()-1; i > -1; i--)
		{
			int currentIndex = i;
			Layer layer 	 = layers.get(i);
			
			if(!layer.canBeLoaded())
				continue;
			
			VBox paneLayer 	 = new VBox();
			HBox paneCon	 = new HBox(5);
			Label lblLayer 	 = new Label(layer.getName());

			paneCon.setAlignment(Pos.CENTER_LEFT);
			boolean layerSelected = layersManager.getSelection().has(i);
			Color backColor = layerSelected ? Color.LIGHTBLUE.brighter().brighter() : Color.WHITE;

			VBox.setMargin(paneLayer, new Insets(0, 0, i == 1 ? 0 : 1, 0));
			paneLayer.setPadding(new Insets(5));
			paneLayer.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,
					CornerRadii.EMPTY, new BorderWidths(0, 0, 1, 0))));
			paneLayer.setBackground(new Background(new BackgroundFill(backColor, CornerRadii.EMPTY, new Insets(0))));
			paneLayer.setCursor(Cursor.HAND);
			
			Pane paneBTNLayerVisibility = new Pane();
			paneBTNLayerVisibility.setBackground(
					layer.canChangeVisibility() ? (
						layer.isVisible() ? IconRegistry.ICON_LAYER_VISIBLE.getBackground() :
						IconRegistry.ICON_LAYER_INVISIBLE.getBackground()) :
					IconRegistry.ICON_LAYER_CCVISIBILITY.getBackground());
			paneBTNLayerVisibility.setPrefSize(16, 16);
			paneBTNLayerVisibility.setMinSize(16, 16);
			
			Tooltip.install(paneBTNLayerVisibility, new Tooltip(Translation.getTranslation("panels.layers.tooltips.layerVisibility")));
			paneCon.getChildren().add(paneBTNLayerVisibility);

			paneBTNLayerVisibility.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					layersManager.selectLayer(currentIndex);
					canvas.getTool().reset();
					
					Layer layer = layersManager.getSelectedLayer();
					if(layer.canChangeVisibility())
					{
						layer.setVisibility(!layer.isVisible());
						canvas.redraw();
					}
					
					reloadLayers(layers);
					event.consume();
				}
			});

			if(layer instanceof ImageLayer)
			{
				ImageLayer imageLayer = (ImageLayer) layer;
				
				double imageWidth  = imageLayer.getWidth(1);
				double imageHeight = imageLayer.getHeight(1);
				
				double correctedWidth  = Math.ceil(landscape ? this.previewWidth : previewHeight * backgroundRatio);
				double correctedHeight = Math.ceil(landscape ? previewWidth * backgroundRatio : this.previewHeight);
				
				HBox boxLayerPreview = new HBox();
				Canvas canvasLayerPreview = new Canvas(correctedWidth, correctedHeight);
				boxLayerPreview.setBorder(new Border(new BorderStroke(
						layerSelected && !imageLayer.isMaskSelected() ? Color.BLACK : Color.TRANSPARENT,
						BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));				
				boxLayerPreview.setPrefSize(canvasLayerPreview.getWidth(), canvasLayerPreview.getHeight());
				boxLayerPreview.setMinSize(canvasLayerPreview.getWidth()+2, canvasLayerPreview.getHeight()+2);
				boxLayerPreview.setMaxSize(canvasLayerPreview.getWidth(), canvasLayerPreview.getHeight());
				
				boxLayerPreview.setOnMouseClicked(new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent event)
					{
						layer.selectMask(false);
						
						Tool tool = canvas.getTool();
						if(tool instanceof MaskTool)
							((MaskTool) tool).setInMask(false);
						
						canvas.redraw();
					}
				});
				
				double scaledImageX		 = ((imageLayer.getX(1) - backgroundX)) * (correctedWidth / backgroundWidth);
				double scaledImageY		 = ((imageLayer.getY(1) - backgroundY)) * (correctedHeight / backgroundHeight);
				double scaledImageWidth  = (imageWidth * correctedWidth) / backgroundWidth;
				double scaledImageHeight = (imageHeight * correctedHeight) / backgroundHeight;
				
				GraphicsContext canvasLGC = canvasLayerPreview.getGraphicsContext2D();
				canvasLGC.drawImage(ImageUtils.fillImage(ResourceLoader.loadImage(BackgroundLayer.backgroundPath),
						(int) previewWidth, (int) previewHeight), 0, 0, correctedWidth, correctedHeight);
				canvasLGC.drawImage(imageLayer.getImage(), scaledImageX, scaledImageY, scaledImageWidth, scaledImageHeight);
				
				boxLayerPreview.getChildren().add(canvasLayerPreview);
				paneCon.getChildren().add(boxLayerPreview);
				
				if(imageLayer.getMask() != null)
				{
					HBox boxMaskPreview = new HBox();
					boxMaskPreview.setBorder(new Border(new BorderStroke(
							layer == selectedLayer && imageLayer.isMaskSelected() ? Color.BLACK : Color.TRANSPARENT,
							BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

					Canvas canvasMaskPreview = new Canvas(previewWidth, previewHeight);
					boxMaskPreview.setPrefSize(canvasMaskPreview.getWidth(), canvasMaskPreview.getHeight());
					boxMaskPreview.setMinSize(canvasMaskPreview.getWidth()+2, canvasMaskPreview.getHeight()+2);
					boxMaskPreview.setMaxSize(canvasMaskPreview.getWidth(), canvasMaskPreview.getHeight());
					
					boxMaskPreview.setOnMouseClicked(new EventHandler<MouseEvent>()
					{
						@Override
						public void handle(MouseEvent event)
						{
							layer.selectMask(true);
							
							Tool tool = canvas.getTool();
							if(tool instanceof MaskTool)
								((MaskTool) tool).setInMask(true);
							
							canvas.redraw();
						}
					});
					
					GraphicsContext canvasMGC = canvasMaskPreview.getGraphicsContext2D();
					canvasMGC.drawImage(ImageUtils.fillImage(Color.BLACK,
							(int) previewWidth, (int) previewHeight), 0, 0, correctedWidth, correctedHeight);
					canvasMGC.drawImage(imageLayer.getMask().getMaskImage(),
							scaledImageX, scaledImageY, scaledImageWidth, scaledImageHeight);
					
					boxMaskPreview.getChildren().add(canvasMaskPreview);
					paneCon.getChildren().add(boxMaskPreview);
				}
			}

			paneLayer.setOnMouseClicked(new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent event)
				{
					if(event.getButton() == MouseButton.PRIMARY &&
					   event.getClickCount() == 2)
					{
						TextField txtLayerName = new TextField(layer.getName());
						
						txtLayerName.focusedProperty().addListener(new ChangeListener<Boolean>()
						{
							@Override
							public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
							{
								if(!newValue.booleanValue())
								{
									paneCon.getChildren().remove(txtLayerName);
									
									if(!paneCon.getChildren().contains(lblLayer))
										paneCon.getChildren().add(lblLayer);
								}
							}
						});
						
						txtLayerName.setOnKeyPressed(new EventHandler<KeyEvent>()
						{
							@Override
							public void handle(KeyEvent event)
							{
								if(event.getCode() == KeyCode.ENTER)
								{
									String text = txtLayerName.getText();
									lblLayer.setText(text);
									layer.setName(text);
									
									paneCon.getChildren().remove(txtLayerName);
									
									if(!paneCon.getChildren().contains(lblLayer))
										paneCon.getChildren().add(lblLayer);
								}
							}
						});
						
						paneCon.getChildren().remove(lblLayer);
						paneCon.getChildren().add(txtLayerName);
						txtLayerName.requestFocus();
						
						return;
					}
					
					if(event.getButton() == MouseButton.SECONDARY)
					{
						int index = layers.size() - layersManager.getSelectedLayerIndex() - 1;
						VBox box0 = (VBox) paneLayersList.getChildren().get(index);
						
						box0.setBackground(new Background(new BackgroundFill(Color.WHITE,
								CornerRadii.EMPTY, new Insets(0))));
						
						layersManager.selectLayer(currentIndex);
						canvas.getTool().reset();

						paneLayer.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE.brighter().brighter(),
							CornerRadii.EMPTY, new Insets(0))));

						if(contextMenu != null)
							contextMenu.hide();
						
						contextMenu = new ContextMenu();
						
						MenuItem cmItemRemoveLayer = new MenuItem(Translation.getTranslation("panels.layers.menu.removeLayer"));
						cmItemRemoveLayer.setOnAction(new EventHandler<ActionEvent>()
						{
							@Override
							public void handle(ActionEvent event)
							{
								layersManager.removeLayer(layer);
								canvas.redraw();
								
								reloadLayers(layersManager.getLayers());
							}
						});
						
						MenuItem cmItemAddMask 		= new MenuItem(Translation.getTranslation("panels.layers.menu.addLayerMask"));
						MenuItem cmItemRemoveMask 	= new MenuItem(Translation.getTranslation("panels.layers.menu.removeLayerMask"));
						
						cmItemAddMask.setOnAction(new EventHandler<ActionEvent>()
						{
							@Override
							public void handle(ActionEvent event)
							{
								if(!layer.canHaveMask())
									return;
								
								if(!(layer instanceof ImageLayer))
									return;
								
								ImageLayer ilayer = (ImageLayer) layer;
								ilayer.setMask(new LayerMask(ilayer));
								ilayer.selectMask(true);
								
								Tool tool = canvas.getTool();
								if(tool instanceof MaskTool)
									((MaskTool) tool).setInMask(true);

								reloadLayers(layers);
							}
						});
						
						cmItemRemoveMask.setOnAction(new EventHandler<ActionEvent>()
						{
							@Override
							public void handle(ActionEvent event)
							{
								if(layer.getMask() == null)
									return;
								
								layer.selectMask(false);
								
								Tool tool = canvas.getTool();
								if(tool instanceof MaskTool)
									((MaskTool) tool).setInMask(false);
								
								layer.setMask(null);
								reloadLayers(layers);
								canvas.redraw();
							}
						});

						if(layers.size() > 1)
						contextMenu.getItems().add(cmItemRemoveLayer);

						if(layer.canHaveMask())
						{
							if(contextMenu.getItems().size() > 0)
							contextMenu.getItems().add(new SeparatorMenuItem());
							
							if(layer.getMask() == null)	contextMenu.getItems().add(cmItemAddMask);
							else						contextMenu.getItems().add(cmItemRemoveMask);
						}
						
						if(contextMenu.getItems().size() > 0)
						contextMenu.show(paneLayer, event.getScreenX(), event.getScreenY());
					}
					else
					{
						canvas.getTool().unload();
						canvas.getTool().reset();
						
						Tool tool = canvas.getTool();
						if(tool instanceof MaskTool)
							((MaskTool) tool).setInMask(layer.isMaskSelected());
						
						cmbBlendModes.getSelectionModel().select(layer.getBlendMode());
						
						reloadToolPanel();
						reloadLayers();
					}
					
					canvas.redraw();
				}
			});

			paneCon.getChildren().add(lblLayer);
			paneLayer.getChildren().add(paneCon);
			
			paneLayersList.getChildren().add(paneLayer);
		}
	}
	
	public void recalculatePreviewsSize()
	{
 		double canvasWidth  = canvas.getCanvasWidth();
 		double canvasHeight = canvas.getCanvasHeight();
 		double ratio 		= 1;
 		
 		if(canvasWidth >= canvasHeight)
 		{
 			ratio 			= canvasHeight / canvasWidth;
 			previewHeight 	= (int) (previewFactor * ratio);
 		}
 		else
 		{
 			ratio 			= canvasWidth / canvasHeight;
 			previewWidth 	= (int) (previewFactor * ratio);
 		}
	}
	
	public void addKeyListener(EventHandler<KeyEvent> listener)
	{
		keyListeners.add(listener);
	}
	
	public void removeKeyListener(EventHandler<KeyEvent> listener)
	{
		keyListeners.remove(listener);
	}
	
	public void reloadToolPanel()
	{
		this.paneTool.getChildren().clear();
		this.paneTool.getChildren().add(canvas.getTool().init());
	}
	
	public CanvasPanel getCanvas()
	{
		return canvas;
	}
	
	public IScrollPane getScrollPane()
	{
		return scrollPane;
	}
	
	public ColorPalettePanel getColorPicker()
	{
		return colorPalettePanel;
	}
	
	private Timer timer = new Timer(5000, new ActionListener()
	{
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			setStatus("...");
		}
	});
	
	public void setStatus(String text)
	{
		if(timer.isRepeats())	timer.setRepeats(false);
		if(!timer.isRunning())	timer.start();
		else					timer.restart();
		
		Utils.run(() ->
		{
			lblStatus.setText(text);			
		});
	}
	
	public void setToolInfo(String text)
	{
		Utils.run(() ->
		{
			lblToolInfo.setText(text);
		});
	}
	
	public void stepBackward()
	{
		if(ProgressBackups.hasPreviousBackup())
		{
			ProgressBackup backup;
			if((backup = ProgressBackups.getPreviousBackup()) != null)
				loadBackup(backup);
		}
		else
		{
			if(ProgressBackups.getCurrentPosition() != 0)
				ProgressBackups.createBackup(Editor.this);
		}
	}
	
	public void stepForward()
	{
		if(ProgressBackups.hasNextBackup())
		{
			ProgressBackup backup;
			if((backup = ProgressBackups.getNextBackup()) != null)
				loadBackup(backup);
		}
	}
	
	public byte[] buildProgressBackupFileContent()
	{
		List<Byte> byteArray = new ArrayList<>();
		
		SSDFCore ssdf 	= new SSDFCore();
		SSDArray array 	= new SSDArray("layers");
		
		LayersManager layersManager = canvas.getLayersManager();
		List<Layer> layers			= layersManager.getLayers();
		
		List<byte[]> listImageBytes = new ArrayList<>();
		
		double backgroundX = canvas.getBackgroundLayer().getX(1);
		double backgroundY = canvas.getBackgroundLayer().getY(1);
		
		for(int i = 0; i < layers.size(); i++)
		{
			Layer layer = layers.get(i);
			SSDArray arrayLayer = new SSDArray("layer" + Integer.toString(i));
			arrayLayer.setObject("name", layer.getName());
			arrayLayer.setObject("selected", layersManager.getSelectedLayer() == layer);
			arrayLayer.setObject("hasMask", layer.getMask() != null);
			
			if(layer instanceof ImageLayer)
			{
				try
				{
					ImageLayer imageLayer = (ImageLayer) layer;
					BufferedImage buffImage = SwingFXUtils.fromFXImage(imageLayer.getImage(), null);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					ImageIO.write(buffImage, "png", stream);
					
					byte[] imageBytes = stream.toByteArray();
					listImageBytes.add(imageBytes);
					
					arrayLayer.setObject("image", i);
					arrayLayer.setObject("offsetX", (imageLayer.getX(1) - backgroundX));
					arrayLayer.setObject("offsetY", (imageLayer.getY(1) - backgroundY));
					arrayLayer.setObject("width", imageLayer.getWidth(1));
					arrayLayer.setObject("height", imageLayer.getHeight(1));
				}
				catch(Exception ex) { ex.printStackTrace(); }
			}
			
			array.setArray(arrayLayer);
		}
		
		ssdf.setArray(array);
		
		String content = ssdf.getContentString();
		ByteHelper.appendBytes(byteArray, content.getBytes());
		ByteHelper.appendBytes(byteArray, ProgressBackups.HEADER_DELIMITER);
		
		// Save images
		for(byte[] imageArray : listImageBytes)
		{
			ByteHelper.appendBytes(byteArray, ProgressBackups.IMAGE_START_DELIMITER);
			ByteHelper.appendBytes(byteArray, imageArray);
			ByteHelper.appendBytes(byteArray, ProgressBackups.IMAGE_END_DELIMITER);
		}
		
		return ByteHelper.toByteArray(byteArray);
	}
	
	private class LayerData
	{
		public String name;
		public Map<String, Object> data;
		
		public LayerData(String name)
		{
			this.name = name;
			this.data = new HashMap<>();
		}
	}
	
	private Map<String, LayerData> getLayersData(SSDArray layers)
	{
		Map<String, LayerData> listData = new HashMap<>();
		for(SSDObject object : layers)
		{
			String name 	 = object.getName();
			String[] split 	 = name.split("\\.");
			String layerName = split[1];
			String itemName  = String.join(".", Arrays.copyOfRange(split, 2, split.length));
			
			if(!listData.containsKey(layerName))
				listData.put(layerName, new LayerData(layerName));
			
			listData.get(layerName).data.put(itemName, object.getValue());
		}
		
		return listData;
	}
	
	public void loadBackup(ProgressBackup backup)
	{
		SSDArray data 				= backup.data;
		List<WritableImage> images 	= backup.images;
		
		SSDArray layers 				= data.getArray("layers");
		Map<String, LayerData> listData = getLayersData(layers);
		
		LayersManager layersManager = canvas.getLayersManager();
		layersManager.clear();
		
		for(Entry<String, LayerData> entry : listData.entrySet())
		{
			try
			{
				LayerData layerData = entry.getValue();
				int index = Integer.parseInt(layerData.data.get("image").toString());
				
				double posX 	= Double.parseDouble(layerData.data.get("offsetX").toString());
				double posY 	= Double.parseDouble(layerData.data.get("offsetY").toString());
				double width 	= Double.parseDouble(layerData.data.get("width").toString());
				double height 	= Double.parseDouble(layerData.data.get("height").toString());
				
				WritableImage layerImage = images.get(index);
				ImageLayer imageLayer 	 = new ImageLayer(layerImage,
					canvas.getBackgroundLayer().getX(1) + posX,
					canvas.getBackgroundLayer().getY(1) + posY,
					width, height);
				imageLayer.setName(layerData.name);
				
				layersManager.addLayer(imageLayer);
			}
			catch(Exception ex) {}
		}
		
		canvas.getTool().reset();
		canvas.getTool().unload();
		
		canvas.redraw();
		reloadLayers();
	}
}