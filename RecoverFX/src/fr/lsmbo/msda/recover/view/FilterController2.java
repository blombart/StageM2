package fr.lsmbo.msda.recover.view;

import java.util.Iterator;

import fr.lsmbo.msda.recover.filters.FragmentIntensityFilter;
import fr.lsmbo.msda.recover.filters.HighIntensityThreasholdFilter;
import fr.lsmbo.msda.recover.filters.IdentifiedSpectraFilter;
import fr.lsmbo.msda.recover.filters.IonReporterFilter;
import fr.lsmbo.msda.recover.filters.LowIntensityThreasholdFilter;
import fr.lsmbo.msda.recover.filters.WrongChargeFilter;
import fr.lsmbo.msda.recover.filters.Filter;
import fr.lsmbo.msda.recover.lists.Filters;
import fr.lsmbo.msda.recover.lists.IonReporters;
import fr.lsmbo.msda.recover.model.ComparisonTypes;
import fr.lsmbo.msda.recover.model.ComputationTypes;
import fr.lsmbo.msda.recover.model.IonReporter;
import fr.lsmbo.msda.recover.model.TextFieldConvertor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class FilterController2 {
	private Stage dialogStage;
	
	private ObservableList<String> modeBaselineList = FXCollections.observableArrayList("Average of all peaks","Median of all peaks");
	private ObservableList<String> comparatorIntensity = FXCollections.observableArrayList("=","#",">",">=","<","<=");
	
	
	//instance filters
	private HighIntensityThreasholdFilter filterHIT = new HighIntensityThreasholdFilter() ;
	private LowIntensityThreasholdFilter filterLIT = new LowIntensityThreasholdFilter() ;
	private FragmentIntensityFilter filterFI = new FragmentIntensityFilter();
	private WrongChargeFilter filterWC = new WrongChargeFilter();
	private IdentifiedSpectraFilter filterIS = new IdentifiedSpectraFilter();
	private IonReporterFilter filterIR = new IonReporterFilter();
	
	//Instance control of one filter as a list.
	private ObservableList<Control> controlHIT = FXCollections.observableArrayList();
	private ObservableList<Control> controlLIT = FXCollections.observableArrayList();
	private ObservableList<Control> controlFI = FXCollections.observableArrayList();
	private ObservableList<Control> controlIS = FXCollections.observableArrayList();
	private ObservableList<Control> controlIR = FXCollections.observableArrayList();
	
	private ObservableList<Alert> arrayAlert = FXCollections.observableArrayList();
	
	/*******************************************
	 Control for High Intensity Threshold Filter
	 *******************************************/
	@FXML
	private CheckBox checkBoxHighIntensityThresholdFilter;
	@FXML
	private TextField mostIntensePeaksToConsider;
	@FXML
	private TextField percentageOfTopLine;
	@FXML
	private TextField maxNbPeaks;
	
	/*******************************************
	 Control for low Intensity Threshold Filter
	 *******************************************/
	@FXML
	private CheckBox checkBoxLowIntensityThresholdFilter;
	@FXML
	private ChoiceBox<String> modeBaseline;
	@FXML
	private TextField emergence;
	@FXML
	private TextField minUPN;
	@FXML 
	private TextField maxUPN;
	
	/********************************
	 Control for Fragment Intensity Filter
	 ********************************/
	@FXML
	private CheckBox checkBoxFragmentIntensityFilter;
	@FXML
	private ChoiceBox<String> comparatorFragmentIntensity ;
	@FXML
	private TextField fragmentIntensity;
	
	/********************************
	 Control for Wrong Charge Filter
	 ********************************/
	@FXML
	private CheckBox checkBoxWrongChargeFilter;
	
	/********************************
	 Control for Identified Spectra Filter
	 ********************************/
	@FXML
	private CheckBox checkBoxIdentifiedSpectraFilter;
	@FXML
	private TextArea titles;
//	@FXML
//	private Button buttonIdentifiedSpectra;
	
	/********************************
	 Control for Ion ReporterFilter
	 ********************************/
	@FXML 
	private CheckBox checkBoxIonReporterFilter;
	@FXML
	private TableView<IonReporter> tableIonReporter;
	@FXML
	private TableColumn<IonReporter, Float> colMoz;
	@FXML
	private TableColumn<IonReporter, Float> colTolerance;
	@FXML
	private TableColumn<IonReporter, String> colName;
//	@FXML
//	private Button buttonIonReporter;
	@FXML
	private TextField mozIonReporter;
	@FXML
	private TextField toleranceIonReporter;
	@FXML
	private TextField nameIonReporter;
	@FXML
	private Button buttonInsertIonReporter;
	@FXML
	private Button buttonResetIonReporter;
	
	//Buttons
	@FXML 
	private Button btnApply;
	@FXML 
	private Button btnCancel;
	
	@FXML
	private void initialize(){
		//collect the control (text field, button, etc..) for a specific filter in a list.
		controlHIT.addAll(mostIntensePeaksToConsider,percentageOfTopLine,maxNbPeaks);
		controlLIT.addAll(modeBaseline, emergence, minUPN, maxUPN);
		controlFI.addAll(comparatorFragmentIntensity, fragmentIntensity);
		controlIS.addAll(titles);
		controlIR.addAll(tableIonReporter, mozIonReporter, toleranceIonReporter, nameIonReporter, buttonInsertIonReporter, buttonResetIonReporter);
	
		//Disable all the control at the initialization.	
		setDisableControl(controlHIT,"disable");
		setDisableControl(controlLIT,"disable");
		setDisableControl(controlFI,"disable");
		setDisableControl(controlIS,"disable");
		setDisableControl(controlIR,"disable");
		
		//Initialize values for the two choice box (LIT and FI) and set the first value ("average" && "=" )
		modeBaseline.setItems(modeBaselineList);
		modeBaseline.getSelectionModel().selectFirst();
		
		comparatorFragmentIntensity.setItems(comparatorIntensity);
		comparatorFragmentIntensity.getSelectionModel().selectFirst();
		
		//Initialize table view for Ion Reporter
		tableIonReporter.setItems(IonReporters.getIonReporters());
		colMoz.setCellValueFactory(new PropertyValueFactory<IonReporter, Float>("moz"));
		colTolerance.setCellValueFactory(new PropertyValueFactory<IonReporter, Float>("tolerance"));
		colName.setCellValueFactory(new PropertyValueFactory<IonReporter, String>("name"));
		
		//Keep in memory parameters passed previously
		if(Filters.nbFilterUsed() !=0)
			
			//initialize previous values of the filterHIT
			if ((Filters.getFilters().get("HIT"))!=null){
				filterHIT = (HighIntensityThreasholdFilter) Filters.getFilters().get("HIT");
				checkBoxHighIntensityThresholdFilter.setSelected(true);
				checkHighIntensityThresholdFilter();
				mostIntensePeaksToConsider.setText(Integer.toString(filterHIT.getNbMostIntensePeaksToConsider()));
				percentageOfTopLine.setText(Float.toString(filterHIT.getPercentageOfTopLine()));
				maxNbPeaks.setText(Integer.toString(filterHIT.getMaxNbPeaks()));
			}
		
			//initialize previous values of the filterLIT
			if ((Filters.getFilters().get("LIT"))!=null){
				filterLIT = (LowIntensityThreasholdFilter) Filters.getFilters().get("LIT");
				checkBoxLowIntensityThresholdFilter.setSelected(true);
				checkLowIntensityThresholdFilter();
				emergence.setText(Integer.toString(filterLIT.getEmergence()));
				minUPN.setText(Integer.toString(filterLIT.getMinUPN()));
				maxUPN.setText(Integer.toString(filterLIT.getMaxUPN()));
				if (filterLIT.getMode() == ComputationTypes.MEDIAN)
					modeBaseline.getSelectionModel().selectLast();
			}
			
			//initialize previous values of the filterFI
			if  ((Filters.getFilters().get("FI"))!=null){
				filterFI = (FragmentIntensityFilter) Filters.getFilters().get("FI");
				checkBoxFragmentIntensityFilter.setSelected(true);
				checkFragmentIntensityFilter();
				fragmentIntensity.setText(Integer.toString(filterFI.getIntensityFragment()));
				comparatorFragmentIntensity.getSelectionModel().select(setIntegerToFragmentComparator());
			}
			
			if  ((Filters.getFilters().get("WC"))!=null){
				checkBoxWrongChargeFilter.setSelected(true);
			}
			
			//initialize previous values of the filterIS
			if  ((Filters.getFilters().get("IS"))!=null){
				filterIS = (IdentifiedSpectraFilter) Filters.getFilters().get("IS");
				checkBoxIdentifiedSpectraFilter.setSelected(true);
				checkIdentifiedSpectraFilter();
				String allTitle = "";
				for (String st : filterIS.getArrayTitles()){
					allTitle += st + "\n";
				}
				titles.setText(allTitle);
			}
			
			if  ((Filters.getFilters().get("IR"))!=null){
				filterIR = (IonReporterFilter) Filters.getFilters().get("IR");
				checkBoxIonReporterFilter.setSelected(true);
				checkIonReporterFilter();
			}
	}
	
	@FXML
	private void handleClickBtnApply(){
		Filter.redoFromTheBeginning();
		
		//If the filter is selected, check the value (display an alert is the given value is false) and set the parameters for the given filter.
		
		//Verification and parameterization for the filter HIT
		if (checkBoxHighIntensityThresholdFilter.isSelected()){
			try{
				//Convert value of the parameters in the text area.
			Integer mostIntensePeaksToConsiderInt = TextFieldConvertor.changeTextFieldToInteger(mostIntensePeaksToConsider);
			Integer maxNbPeaksInt = TextFieldConvertor.changeTextFieldToInteger(maxNbPeaks);
			Float percentageOfTopLineFloat = TextFieldConvertor.changeTextFieldToFloat(percentageOfTopLine);
			
			//Check the value of percentage (need to be between 0 and 1) and display an alert
			if(percentageOfTopLineFloat < 0 || percentageOfTopLineFloat > 1){
				Alert alert = new Alert(AlertType.WARNING);
				arrayAlert.add(alert);
				alert.setTitle("Bad percentage");
				alert.setHeaderText("Please enter a value (float) between 0 and 1 for percentage");
				alert.showAndWait();
			}
			filterHIT.setParameters(mostIntensePeaksToConsiderInt, percentageOfTopLineFloat, maxNbPeaksInt);

		}catch (NumberFormatException e){
			Alert alert = new Alert(AlertType.WARNING);
			arrayAlert.add(alert);
			alert.setTitle("No numeric parameters have been chosen");
			alert.setHeaderText("Please enter numeric values for Most intense peaks/Number peaks and float for percentage");
			alert.showAndWait();
		}
			Filters.add("HIT", filterHIT);
		}
		
		else{
			Filters.add("HIT",null);
		}
		
		//Verification and parameterization for the filter LIT
		if (checkBoxLowIntensityThresholdFilter.isSelected()){
			try{
				Integer emergenceInt = TextFieldConvertor.changeTextFieldToInteger(emergence);
				Integer minUPNInt = TextFieldConvertor.changeTextFieldToInteger(minUPN);
				Integer maxUPNInt = TextFieldConvertor.changeTextFieldToInteger(maxUPN);
				
				filterLIT.setParameters(emergenceInt,minUPNInt, maxUPNInt, ComputationTypes.setChoiceMode(modeBaseline));
		}catch (NumberFormatException e){
			Alert alert = new Alert(AlertType.WARNING);
			arrayAlert.add(alert);
			alert.setTitle("No numeric parameters have been chosen");
			alert.setHeaderText("Please enter a numeric value for Emergence/Min useful peaks/Max useful peaks");
			alert.showAndWait();
		}
			Filters.add("LIT", filterLIT);
		}
		
		else{
			Filters.add("LIT", null);
		}
		
		//Verification and parameterization for the filter FI
		if (checkBoxFragmentIntensityFilter.isSelected()){
			try{
			Integer intensityInt = TextFieldConvertor.changeTextFieldToInteger(fragmentIntensity);
			filterFI.setParameters(intensityInt, ComparisonTypes.setChoiceComparator(comparatorFragmentIntensity));
			}catch (NumberFormatException e){
				Alert alert = new Alert(AlertType.WARNING);
				arrayAlert.add(alert);
				alert.setTitle("No numeric parameters have been chosen");
				alert.setHeaderText("Please enter a numeric value for intensity");
				alert.showAndWait();
			}
			Filters.add("FI", filterFI);
		}
		else{
			Filters.add("FI", null);
		}
		
		//filter WC
		if(checkBoxWrongChargeFilter.isSelected()){
			Filters.add("WC", filterWC);
		}
		
		
		//Verification and parameterization for the filter IS
		if(checkBoxIdentifiedSpectraFilter.isSelected()){
			String [] arrayTitles = titles.getText().split("\n");
			filterIS.setParameters(arrayTitles);
			
			Filters.add("IS", filterIS);
		}
		else{
			Filters.add("IS",null);
		}
		
		
		//Verification and parameterization for the filter IR
		if(checkBoxIonReporterFilter.isSelected()){
			Filters.add("IR", filterIR);
		}
		
		else{
			Filters.add("IR", null);
			IonReporters.getIonReporters().clear();
		}
		
		//close the windows only if there isn't alert.
		if (arrayAlert.size() == 0){
			Filter filter = new Filter();
			filter.applyFilters();
			dialogStage.close();
		}
		else
			arrayAlert.clear();
	}
	
	@FXML
	private void checkHighIntensityThresholdFilter(){
		
		if (checkBoxHighIntensityThresholdFilter.isSelected()){
			setDisableControl(controlHIT, "enable");
		}
		
		else
			setDisableControl(controlHIT, "disable");
	}
	
	@FXML
	private void checkLowIntensityThresholdFilter(){
		
		if (checkBoxLowIntensityThresholdFilter.isSelected()){
			setDisableControl(controlLIT,"enable");
		}
		
		else
			setDisableControl(controlLIT, "disable");
	}
	
	@FXML
	private void checkFragmentIntensityFilter(){
		
		if (checkBoxFragmentIntensityFilter.isSelected()){
			setDisableControl(controlFI, "enable");
		}
		
		else
			setDisableControl(controlFI, "disable");
	}
	
	@FXML
	private void checkIdentifiedSpectraFilter(){
		
		if (checkBoxIdentifiedSpectraFilter.isSelected()){
			setDisableControl(controlIS, "enable");
		}
		
		else
			setDisableControl(controlIS, "disable");
	}
	
	@FXML
	private void checkIonReporterFilter(){
		
		if (checkBoxIonReporterFilter.isSelected()){
			setDisableControl(controlIR, "enable");
		}
		
		else
			setDisableControl(controlIR, "disable");
	}
	
	@FXML
	private void insertIonToTableView(){
		
		Float mozIonReporterFloat = TextFieldConvertor.changeTextFieldToFloat(mozIonReporter);
		Float toleranceIonReporterFloat = TextFieldConvertor.changeTextFieldToFloat(toleranceIonReporter);
		IonReporters.add(new IonReporter(nameIonReporter.getText(),mozIonReporterFloat,toleranceIonReporterFloat));
		tableIonReporter.refresh();
	}
	
	@FXML
	private void resetIonToTableView(){
		IonReporters.getIonReporters().clear();
	}
	
	@FXML
	private void handleClickBtnCancel(){
		// TODO close window
		dialogStage.close();
	}
	
	//Scan control of a filter and set the property disable or enable.
	public void setDisableControl(ObservableList<Control> control, String string){
		Iterator<Control> itrControl = control.iterator();
		while(itrControl.hasNext()){
			if (string == "disable")
				itrControl.next().setDisable(true);
			else if (string =="enable")
				itrControl.next().setDisable(false);
		}
	}
	
	//link the comparisonTypes with the corresponding index in the collection
	private Integer setIntegerToFragmentComparator(){
		Integer intComparator = 0;
		if (filterFI.getComparator() == ComparisonTypes.NOT_EQUALS_TO)
			intComparator = 1;
		if (filterFI.getComparator() == ComparisonTypes.GREATER_THAN)
			intComparator = 2;
		if (filterFI.getComparator() == ComparisonTypes.GREATER_OR_EQUAL)
			intComparator = 3;
		if (filterFI.getComparator() == ComparisonTypes.LOWER_THAN)
			intComparator = 4;
		if (filterFI.getComparator() == ComparisonTypes.LOWER_OR_EQUAL)
			intComparator = 5;
		return intComparator;
	}
	
	public void setDialogStage(Stage dialogStage){
		this.dialogStage = dialogStage;
	}
}
