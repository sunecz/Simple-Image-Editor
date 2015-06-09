/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sune.apps.ie.registry.Configuration;
import sune.apps.ie.registry.PathSystem;
import sune.apps.ie.registry.Registry;
import sune.apps.ie.translation.Translation;
import sune.apps.ie.translation.Translation.TranslationObject;
import sune.apps.ie.util.Utils;
import sune.utils.ssdf.SSDArray;

public class WindowConfiguration extends Window
{
	private BorderPane pane;
	private VBox box;
	private HBox boxBottom;
	private Label lblLanguage;
	private ComboBox<TranslationObject> cmbLanguages;
	private CheckBox chbCheckUpdates;
	private TableView<Shortcut> tableShortcuts;
	private TableColumn<Shortcut, String> tableColDesc;
	private TableColumn<Shortcut, String> tableColKeys;
	private Button btnSave;
	
	private boolean showDialog;
	private ObservableList<Shortcut> shortcuts
		= FXCollections.observableArrayList();
	
	public class Shortcut
	{
		private final SimpleStringProperty desc;
		private final SimpleStringProperty keys;
		
		public Shortcut(String desc, String keys)
		{
			this.desc = new SimpleStringProperty(desc);
			this.keys = new SimpleStringProperty(keys);
		}
		
		public void setDesc(String value)
		{
			desc.set(value);
		}
		
		public void setKeys(String value)
		{
			keys.set(value);
		}
		
		public String getDesc()
		{
			return desc.get();
		}
		
		public String getKeys()
		{
			return keys.get();
		}
	}
	
	public WindowConfiguration(Editor editor)
	{
		this.pane = new BorderPane();
		this.scene = new Scene(pane, 400, 400);
		
		this.scene.getStylesheets().add(
				"file:///" + 
				PathSystem.getFullPath(
					PathSystem.STYLES_FOLDER +
					Configuration.getStringProperty("style")));
		this.stage.initModality(Modality.APPLICATION_MODAL);
		this.stage.initOwner(editor.getStage());
		
		this.stage.setScene(scene);
		this.stage.getIcons().add(Utils.APP_ICON);
		this.stage.setTitle(Translation.getTranslation("windows.windowConfiguration.title"));
		this.stage.setResizable(false);
		
		this.box 		= new VBox(5);
		this.boxBottom 	= new HBox(5);
		
		this.box.setPadding(new Insets(10));
		this.boxBottom.setPadding(new Insets(0, 10, 10, 10));
		
		this.lblLanguage  = new Label(
			Translation.getTranslation("windows.windowConfiguration.lblLanguage"));
		this.cmbLanguages = new ComboBox<>();
		
		this.lblLanguage.setAlignment(Pos.CENTER_LEFT);
		
		List<TranslationObject> languages = Registry.LANGUAGES.getAll();
		this.cmbLanguages.getItems().addAll(languages.toArray(new TranslationObject[languages.size()]));
		
		int selectedLangIndex = 0;
		String selectedLang   = Configuration.getStringProperty("language");
		List<TranslationObject> translations = Registry.LANGUAGES.getAll();
		for(int i = 0; i < translations.size(); i++)
		{
			TranslationObject object = translations.get(i);
			if(selectedLang.equals(object.mark))
			{
				selectedLangIndex = i;
				break;
			}
		}
		
		TranslationObject selected = translations.get(selectedLangIndex);
		this.cmbLanguages.getSelectionModel().select(selected);
		this.cmbLanguages.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				TranslationObject selectedObject = cmbLanguages.getValue();
				showDialog = selected != selectedObject;
			}
		});
		
		this.chbCheckUpdates = new CheckBox(
			Translation.getTranslation("windows.windowConfiguration.chbCheckUpdates"));
		this.chbCheckUpdates.setSelected(Configuration.getBooleanProperty("checkUpdates"));
		
		this.btnSave = new Button(Translation.getTranslation("windows.windowConfiguration.btnSave"));
		this.btnSave.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				Configuration.setStringProperty("language", cmbLanguages.getSelectionModel().getSelectedItem().mark);
				Configuration.setStringProperty("checkUpdates", Boolean.toString(chbCheckUpdates.isSelected()));
				
				Configuration.save();
				if(showDialog)
				{
					Alert alert = new Alert(AlertType.CONFIRMATION,
						Translation.getTranslation("messages.configuration.languageChanged.message"),
						ButtonType.OK);
					
					Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
					stage.getIcons().add(Utils.APP_ICON);
					alert.setTitle(Translation.getTranslation("messages.configuration.languageChanged.title"));
					alert.setHeaderText(Translation.getTranslation("messages.configuration.languageChanged.headerText"));
					alert.initOwner(WindowConfiguration.this.stage);
					alert.showAndWait();
				}
				
				close();
			}
		});
		
		List<SSDArray> shorcutsArray
			= Translation.getTranslationArray("shortcuts");
		
		for(SSDArray array : shorcutsArray)
			shortcuts.add(new Shortcut(
				array.getObject("name").getValue(),
				array.getObject("value").getValue()));
		
		this.tableShortcuts = new TableView<>();
		this.tableColDesc = new TableColumn<>(Translation.getTranslation("misc.description"));
		this.tableColKeys = new TableColumn<>(Translation.getTranslation("misc.shortcut"));
		this.tableColDesc.setPrefWidth(200);
		this.tableColKeys.setPrefWidth(120);
		
		this.tableColDesc.setCellValueFactory(new PropertyValueFactory<Shortcut, String>("desc"));
		this.tableColKeys.setCellValueFactory(new PropertyValueFactory<Shortcut, String>("keys"));
		
		this.tableShortcuts.getColumns().add(tableColDesc);
		this.tableShortcuts.getColumns().add(tableColKeys);
		this.tableShortcuts.setEditable(false);
		this.tableShortcuts.setItems(shortcuts);
		VBox.setMargin(tableShortcuts, new Insets(10, 0, 0, 0));
		
		HBox hbox = new HBox(5);
		hbox.getChildren().add(lblLanguage);
		HBox p0 = new HBox();
		HBox.setHgrow(p0, Priority.ALWAYS);
		hbox.getChildren().add(p0);
		hbox.getChildren().add(cmbLanguages);
		hbox.setAlignment(Pos.CENTER_LEFT);
		
		cmbLanguages.setPrefWidth(150);
		cmbLanguages.setMinWidth(ComboBox.USE_PREF_SIZE);
		HBox.setHgrow(cmbLanguages, Priority.ALWAYS);
		
		this.box.getChildren().add(hbox);
		this.box.getChildren().add(chbCheckUpdates);
		this.box.getChildren().add(tableShortcuts);
		
		HBox p1 = new HBox();
		HBox.setHgrow(p1, Priority.ALWAYS);
		this.boxBottom.getChildren().add(p1);
		this.boxBottom.getChildren().add(btnSave);
		this.pane.setCenter(box);
		this.pane.setBottom(boxBottom);
	}
}