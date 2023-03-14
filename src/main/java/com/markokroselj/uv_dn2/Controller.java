package com.markokroselj.uv_dn2;


import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    public TitledPane calculator;
    public TitledPane calcHistoryPane;
    public Accordion accordion;
    public Label runtimeVer;
    public Label statusBarLabel;
    public HBox statusBar;
    public ImageView javaIcon;
    public TextField calculatorDisplay;
    public MenuBar menuBar;
    public TextArea calculationHistoryOutput;
    public TextArea eventLog;
    public MenuItem helpMenuItem;
    public TabPane rightSection;
    public TextArea calculationHistoryOutputSide;
    public TextArea eventLogSide;

    public static final String HELP_URL = "https://cloud.markokroselj.com/uv/dn2/help.pdf";
    private static final boolean LOGGING_ENABLED = true;

    private static String statusBarLabeContent = "";


    boolean wasAuthorMenuItemPressed = false;
    boolean plusMinusPressed = false;
    boolean minus = false;
    boolean operatorWasPressed = false;
    boolean factorialButtonWasPressed = false;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Open calculator pane at startup
        accordion.setExpandedPane(calculator);

        //Install tooltip that displays runtime info
        Tooltip.install(javaIcon, new Tooltip("Runtime version: " + (System.getProperty("java.runtime.version") + " " + System.getProperty("os.arch") + "\nVM: " + System.getProperty("java.vm.name"))));
        runtimeVer.setText(System.getProperty("java.runtime.version") + " " + System.getProperty("os.arch"));

        //Menu bar
        menuBar.getMenus().get(2).getItems().get(1).setOnAction(event -> {
            wasAuthorMenuItemPressed = true;
            setStatusBarLabeContent("Avtor: Marko Krošelj");
        });

        //Only allow numbers and operators to be entered into the calculator input field
        calculatorDisplay.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (!newText.matches("[0-9\\+\\-x/\\(\\)\\\\e\\π\\.\\s+\\!\\*\\/]*")) {
                if (newText.charAt(newText.length() - 1) == '=') onEqualsButtonAction();
                else setStatusBarLabeContent("Ne veljaven vnos: " + newText.charAt(newText.length() - 1));
                return null;
            } else {
                if (newText.length() > 0) {
                    String s = String.valueOf(newText.charAt(newText.length() - 1));
                    if (s.matches("\\d")) {
                        operatorWasPressed = false;
                        factorialButtonWasPressed = false;
                    }
                }

                return change;
            }
        }));

        //Custom context menu for the textarea
        HBox calculationHistoryContextMenuButtons = new HBox();
        Button openContextMenuButton = new Button("Odpri");
        Button saveContextMenuButton = new Button("Shrani");
        Button clearContextMenuButton = new Button("Pobriši");

        calculationHistoryContextMenuButtons.getChildren().addAll(openContextMenuButton, saveContextMenuButton, clearContextMenuButton);
        for (int i = 0; i < calculationHistoryContextMenuButtons.getChildren().size(); i++) {
            calculationHistoryContextMenuButtons.getChildren().get(i).setOnMouseEntered(this::toolBarButtonsMouseEntered);
            calculationHistoryContextMenuButtons.getChildren().get(i).setOnMouseExited(e -> toolBarButtonsMouseExited());

        }
        openContextMenuButton.setOnAction(e -> openCalculationHistory());
        saveContextMenuButton.setOnAction(e -> saveCalculationHistory());
        clearContextMenuButton.setOnAction(e -> clearDisplayAndHistory());

        calculationHistoryContextMenuButtons.getStyleClass().add("contextMenuBtns");
        CustomMenuItem customMenuItem = new CustomMenuItem(calculationHistoryContextMenuButtons);
        customMenuItem.getStyleClass().add("custom-menu-item");

        MenuItem copyCalculationHistoryMenuItem = new MenuItem("Kopiraj");
        MenuItem clearCalculationHistoryMenuItem = new MenuItem("Počisti");
        MenuItem selectAllCalculationHistoryMenuItem = new MenuItem("Izberi vse");

        ContextMenu calculationHistoryOutputContextMenu = new ContextMenu();
        calculationHistoryOutputContextMenu.getStyleClass().add("calc-history-context-menu");
        calculationHistoryOutputContextMenu.getItems().addAll(customMenuItem,
                copyCalculationHistoryMenuItem, clearCalculationHistoryMenuItem,
                new SeparatorMenuItem(), selectAllCalculationHistoryMenuItem);
        calculationHistoryOutput.setContextMenu(calculationHistoryOutputContextMenu);

        copyCalculationHistoryMenuItem.setOnAction(e -> calculationHistoryOutput.copy());
        clearCalculationHistoryMenuItem.setOnAction(e -> calculationHistoryOutput.clear());
        selectAllCalculationHistoryMenuItem.setOnAction(e -> calculationHistoryOutput.selectAll());

        copyCalculationHistoryMenuItem.setDisable(calculationHistoryOutput.getText().isEmpty());
        clearCalculationHistoryMenuItem.setDisable(calculationHistoryOutput.getText().isEmpty());
        selectAllCalculationHistoryMenuItem.setDisable(calculationHistoryOutput.getText().isEmpty());

        calculationHistoryOutput.textProperty().addListener((observable, oldValue, newValue) -> {
            clearCalculationHistoryMenuItem.setDisable(newValue.isEmpty());
            selectAllCalculationHistoryMenuItem.setDisable(newValue.isEmpty());
        });
        calculationHistoryOutput.selectedTextProperty().addListener((observable, oldValue, newValue) -> copyCalculationHistoryMenuItem.setDisable(newValue.isEmpty()));

        MenuItem copyEventLogMenuItem = new MenuItem("Kopiraj");
        MenuItem clearEventLogMenuItem = new MenuItem("Počisti");
        MenuItem selectAllEventLogMenuItem = new MenuItem("Izberi vse");

        ContextMenu eventLogContextMenu = new ContextMenu();
        eventLogContextMenu.getItems().addAll(copyEventLogMenuItem, clearEventLogMenuItem,
                new SeparatorMenuItem(), selectAllEventLogMenuItem);

        eventLog.setContextMenu(eventLogContextMenu);

        copyEventLogMenuItem.setOnAction(e -> eventLog.copy());
        clearEventLogMenuItem.setOnAction(e -> eventLog.clear());
        selectAllEventLogMenuItem.setOnAction(e -> eventLog.selectAll());

        copyEventLogMenuItem.setDisable(eventLog.getText().isEmpty());
        clearEventLogMenuItem.setDisable(eventLog.getText().isEmpty());
        selectAllEventLogMenuItem.setDisable(eventLog.getText().isEmpty());

        eventLog.textProperty().addListener((observable, oldValue, newValue) -> {
            clearEventLogMenuItem.setDisable(newValue.isEmpty());
            selectAllEventLogMenuItem.setDisable(newValue.isEmpty());

        });
        eventLog.selectedTextProperty().addListener((observable, oldValue, newValue) -> copyEventLogMenuItem.setDisable(newValue.isEmpty()));

        //Open help pdf
        helpMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F1));
        helpMenuItem.setOnAction(event -> {
            setStatusBarLabeContent("Odpiranje pomoči...");
            openWebsite(HELP_URL);
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) calculatorDisplay.getScene().getWindow();
            float widthPercent = 0.25f;
            float minStageWidth = 685;
            rightSection.setMinWidth(300);
            rightSection.setPrefWidth(stage.getWidth() * widthPercent);
            rightSection.setVisible(stage.getWidth() > minStageWidth);
            rightSection.setManaged(stage.getWidth() > minStageWidth);

            stage.widthProperty().addListener(((observable, oldValue, newValue) -> {
                rightSection.setMinWidth(300);
                rightSection.setPrefWidth(stage.getWidth() * widthPercent);
                rightSection.setVisible(newValue.intValue() > minStageWidth);
                rightSection.setManaged(newValue.intValue() > minStageWidth);
            }

            ));

        });

        javaIcon.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                if (mouseEvent.getClickCount() == 7) {
                    openWebsite("https://www.java.com/");
                }
            }
        });
    }

    public void toolBarButtonsMouseEntered(MouseEvent mouseEvent) {
        switch (((Button) (mouseEvent.getSource())).getText()) {
            case "Odpri":
                statusBarLabel.setText("Odpri dialog za izbiro datoteke");
                break;
            case "Shrani":
                if (!wasAuthorMenuItemPressed) statusBarLabel.setText("Shrani vsebino dnevnika izračunov v datoteko");
                break;
            case "Zapri":
                statusBarLabel.setText("Zapri Kalkulator");
                break;
            case "Pobriši":
                statusBarLabel.setText("Pobriši vsebino zaslona kalkulatorja in dnevnika izračunov");
        }

    }

    public void toolBarButtonsMouseExited() {
        statusBarLabel.setText(statusBarLabeContent);
        wasAuthorMenuItemPressed = false;
    }

    public void OnNumPadButtonAction(ActionEvent actionEvent) {
        operatorWasPressed = false;
        factorialButtonWasPressed = false;
        calculatorDisplay.setText(calculatorDisplay.getText() + ((Button) actionEvent.getSource()).getText());
    }

    public void onPlusMinusButtonAction() {
        if (operatorWasPressed || isCalculatorDisplayEmpty()) {
            statusBarLabel.setText("Najprej vnesite število!");
            return;
        }
        String pattern = "(?<=\\+)|(?<=-)|(?<=x)|(?<=/)";
        if (!plusMinusPressed) {

            String[] tmp = calculatorDisplay.getText().split(pattern);


            String[] tmp2 = tmp[tmp.length - 1].split(pattern);

            StringBuilder calcDisplayOutput = new StringBuilder();
            for (int i = 0; i < tmp.length - 1; i++) {
                calcDisplayOutput.append(tmp[i]);
            }
            calcDisplayOutput.append(" -").append(tmp2[0].strip());
            calculatorDisplay.setText(calcDisplayOutput.toString());
            plusMinusPressed = true;
            minus = true;
        } else {

            if (minus) {
                String currentCalcDisplay = calculatorDisplay.getText();
                String[] tmp = currentCalcDisplay.split(pattern);
                StringBuilder calcDisplayOutput = new StringBuilder();
                for (int i = 0; i < tmp.length - 2; i++) {
                    calcDisplayOutput.append(tmp[i]);
                }
                calcDisplayOutput.append(" ");
                calcDisplayOutput.append(tmp[tmp.length - 1]);
                calculatorDisplay.setText(calcDisplayOutput.toString());
                minus = false;
            } else {
                String[] tmp = calculatorDisplay.getText().split(pattern);


                String[] tmp2 = tmp[tmp.length - 1].split(pattern);

                StringBuilder calcDisplayOutput = new StringBuilder();
                for (int i = 0; i < tmp.length - 1; i++) {
                    calcDisplayOutput.append(tmp[i]);
                }
                calcDisplayOutput.append(" -").append(tmp2[0].strip());
                calculatorDisplay.setText(calcDisplayOutput.toString());
                minus = true;
            }

        }
    }

    public void onEqualsButtonAction() {
        String calculatorDisplayContent = calculatorDisplay.getText().strip();
        if (calculatorDisplayContent.equals("")) {
            setStatusBarLabeContent("Prvo vnesite račun!");
            return;
        }

        if (operatorWasPressed) {
            setStatusBarLabeContent("Vnesite število, da dopolnite račun!");
            return;
        }

        String pi = String.valueOf(new BigDecimal(Math.PI).setScale(7, RoundingMode.HALF_UP));
        String e = String.valueOf(new BigDecimal(Math.E).setScale(7, RoundingMode.HALF_UP));

        if (calculatorDisplayContent.strip().equals("π")) {
            calculatorDisplay.setText(pi);
            addToCalculationHistory(calculatorDisplayContent);
            setStatusBarLabeContent("Uspešen izračun!");
            resetBooleans();
            calculatorDisplay.positionCaret(calculatorDisplay.getText().length());

            return;

        }

        if (calculatorDisplayContent.strip().equals("e")) {
            calculatorDisplay.setText(e);
            addToCalculationHistory(calculatorDisplayContent);
            setStatusBarLabeContent("Uspešen izračun!");
            resetBooleans();
            calculatorDisplay.positionCaret(calculatorDisplay.getText().length());

            return;

        }
        calculatorDisplay.positionCaret(calculatorDisplay.getText().length());

        if (calculatorDisplayContent.matches("\\d+!")) {
            calculatorDisplayContent = calculatorDisplayContent.replace("!", "");
            try {
                calculatorDisplayContent = String.valueOf(factorial(Long.parseLong(calculatorDisplayContent.strip())));
                addToCalculationHistory(calculatorDisplayContent);
                setStatusBarLabeContent("Uspešen izračun!");
            } catch (StackOverflowError stackOverflowError) {
                setStatusBarLabeContent(stackOverflowError.toString());
                resetBooleans();
                return;
            }
            calculatorDisplay.setText(calculatorDisplayContent);
            calculatorDisplay.positionCaret(calculatorDisplay.getText().length());


            return;
        }
        StringBuilder output = new StringBuilder();
        Pattern pattern = Pattern.compile("(\\d+)!");
        Matcher matcher = pattern.matcher(calculatorDisplayContent);

        while (matcher.find()) {
            try {
                matcher.appendReplacement(output, Long.toString(factorial(Long.parseLong(matcher.group(1)))));

            } catch (StackOverflowError stackOverflowError) {
                setStatusBarLabeContent(stackOverflowError.toString());
                resetBooleans();
                return;
            }
        }

        matcher.appendTail(output);

        calculatorDisplayContent = output.toString().strip();

        Object result;
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        try {

            result = engine.eval(calculatorDisplayContent.replace("x", "*").replace("π", pi)
                    .replace("e", e));
            addToCalculationHistory(calculatorDisplayContent);

        } catch (ScriptException scriptException) {
            setStatusBarLabeContent("Ne veljaven vnos!\n" + scriptException);
            resetBooleans();
            return;
        }
        calculatorDisplay.setText(result.toString());
        setStatusBarLabeContent("Uspešen izračun!");
        resetBooleans();
        calculatorDisplay.positionCaret(calculatorDisplay.getText().length());

    }

    public void onOperatorButtonAction(ActionEvent actionEvent) {
        if (calculatorDisplay.getText().strip().endsWith("(")) return;
        if (operatorWasPressed || isCalculatorDisplayEmpty()) return;
        String pressedButtonText = ((Button) actionEvent.getSource()).getText();
        if (pressedButtonText.equals("÷")) pressedButtonText = "/";
        calculatorDisplay.setText(calculatorDisplay.getText() + " " + pressedButtonText + " ");
        plusMinusPressed = false;
        operatorWasPressed = true;
    }

    public void onParenthesesButtonAction(ActionEvent actionEvent) {
        String pressedButtonText = ((Button) actionEvent.getSource()).getText();
        calculatorDisplay.setText(calculatorDisplay.getText() + pressedButtonText);

    }

    public void onPiButtonAction() {
        if (!operatorWasPressed && !isCalculatorDisplayEmpty()) return;
        calculatorDisplay.setText(calculatorDisplay.getText() + "π");
        operatorWasPressed = false;
        factorialButtonWasPressed = false;
    }

    public void onEButtonAction() {
        if (!operatorWasPressed && !isCalculatorDisplayEmpty()) return;
        calculatorDisplay.setText(calculatorDisplay.getText() + "e");
        operatorWasPressed = false;
        factorialButtonWasPressed = false;
    }

    private void resetBooleans() {
        plusMinusPressed = false;
        minus = false;
        operatorWasPressed = false;
    }

    public void clear() {
        resetBooleans();
        calculatorDisplay.clear();
        setStatusBarLabeContent("Vsebina kalkulatorja pobrisana!");
    }

    public void clearDisplayAndHistory() {
        resetBooleans();
        calculatorDisplay.clear();
        calculationHistoryOutput.clear();
        calculationHistoryOutputSide.clear();
        setStatusBarLabeContent("Vsebina zaslona kalkulatorja in dnevnika izračunov pobrisana!");
    }

    private boolean isCalculatorDisplayEmpty() {
        return calculatorDisplay.getText().equals("");
    }

    public long factorial(long n) throws StackOverflowError {
        if (n == 0) return 1;
        try {
            return n * factorial(n - 1);

        } catch (StackOverflowError stackOverflowError) {
            throw new StackOverflowError();
        }
    }

    public void onFactorialButtonPressed() {
        if (operatorWasPressed || isCalculatorDisplayEmpty()) {
            setStatusBarLabeContent("Najprej vnesite število!");
        } else if (!factorialButtonWasPressed) {
            calculatorDisplay.setText(calculatorDisplay.getText().strip() + "!");
            factorialButtonWasPressed = true;

        }

    }

    public void exit() {
        System.exit(0);
    }


    public void onBackspaceButtonAction() {
        String calculatorDisplayContent = calculatorDisplay.getText().strip();
        if (calculatorDisplayContent.equals("")) {
            setStatusBarLabeContent("Nič za izbrisat!");
            return;
        }
        if (String.valueOf(calculatorDisplayContent.charAt(calculatorDisplayContent.length() - 1)).matches("\\+|-|x|/")) {
            operatorWasPressed = false;
        }
        if (minus) {
            if (calculatorDisplayContent.length() > 1)
                calculatorDisplayContent = calculatorDisplayContent.substring(0, calculatorDisplayContent.length() - 2).strip();
            else calculatorDisplayContent = "";

            minus = false;
        } else {
            calculatorDisplayContent = calculatorDisplayContent.substring(0, calculatorDisplayContent.length() - 1).strip();

        }
        calculatorDisplay.setText(calculatorDisplayContent);
        if (calculatorDisplayContent.length() > 0)
            if (String.valueOf(calculatorDisplayContent.charAt(calculatorDisplayContent.length() - 1)).matches("\\+|-|x|/"))
                operatorWasPressed = true;

    }

    public void onDecimalDotAction() {
        String calculatorDisplayContent = calculatorDisplay.getText().strip();
        if (calculatorDisplayContent.equals("")) return;
        if (String.valueOf(calculatorDisplayContent.charAt(calculatorDisplayContent.length() - 1)).matches("\\d+")) {
            calculatorDisplayContent += ".";
            calculatorDisplay.setText(calculatorDisplayContent);
        }

    }

    public void addToCalculationHistory(String calculation) {
        calculationHistoryOutputSide.appendText(calculation + "\n");
        calculationHistoryOutput.appendText(calculation + "\n");
    }

    public void saveCalculationHistory() {
        if (calculationHistoryOutput.getText().isEmpty()) {
            setStatusBarLabeContent("Zgodovina računskih operacij prazna!");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy HH-mm-ss");
        String formattedDateTime = now.format(formatter);

        fileChooser.setInitialFileName("Zgodovina računskih operacij " + formattedDateTime);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("txt (*.txt)", "*.txt"));
        File defaultDirectory = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Documents");
        if (!defaultDirectory.exists()) defaultDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(defaultDirectory);
        setStatusBarLabeContent("Odpiranje dialoga za shranjevanje datoteke...");
        File file = fileChooser.showSaveDialog(calculatorDisplay.getScene().getWindow());
        if (file == null) {
            setStatusBarLabeContent("Uporabnik preklical shranjevanje zgodovine računskih operacij!");
        } else {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write("com.markokroselj.uv_dn2\n");
                fileWriter.write(calculationHistoryOutput.getText());
                setStatusBarLabeContent("Zgodovina računskih operacij uspešno shranjena!");
            } catch (IOException e) {
                setStatusBarLabeContent("Napaka pri shranjevanju zgodovine računskih operacij!");
            }
        }
    }

    public void setStatusBarLabeContent(String content) {
        statusBarLabeContent = content;
        statusBarLabel.setText(statusBarLabeContent);
        if (LOGGING_ENABLED) {
            eventLog.appendText(statusBarLabeContent + "\t" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm:ss")) + "\n");
            eventLogSide.appendText(statusBarLabeContent + "\t" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm:ss")) + "\n");
        }

    }

    public void onCalculatorDisplayAction() {
        onEqualsButtonAction();
    }

    public void openCalculationHistory() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt (*.txt)", "*.txt"));

        File defaultDirectory = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Documents");
        if (!defaultDirectory.exists()) defaultDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(defaultDirectory);
        setStatusBarLabeContent("Odpiranje dialoga za izbiro datoteke...");
        File file = fileChooser.showOpenDialog(calculatorDisplay.getScene().getWindow());

        if (file == null) {
            setStatusBarLabeContent("Uporabnik preklical odpiranje zgodovine računskih operacij!");
        } else {
            try (Scanner scanner = new Scanner(file)) {

                String firstLine = "";
                if (scanner.hasNextLine()) {
                    firstLine = scanner.nextLine();
                }

                if (!firstLine.strip().equals("com.markokroselj.uv_dn2")) {
                    setStatusBarLabeContent("Izbrana datoteka ne vsebuje zgodovine računskih operacij!");
                    return;
                }
                calculationHistoryOutput.clear();
                calculationHistoryOutputSide.clear();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine() + "\n";
                    calculationHistoryOutput.appendText(line);
                    calculationHistoryOutputSide.appendText(line);
                }
                setStatusBarLabeContent("Zgodovina računskih operacij uspešno prebrana!\n" + file.getName() + " " + file.length() + "b");
                if (!accordion.getExpandedPane().equals(calcHistoryPane) && !isSidePaneVisible())
                    accordion.setExpandedPane(calcHistoryPane);
            } catch (IOException e) {
                setStatusBarLabeContent("Napaka pri branju zgodovine računskih operacij!");
            }
        }
    }

    public static void openWebsite(final String url) {
        //Open website in the default web browser
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (URISyntaxException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isSidePaneVisible() {
        return rightSection.isVisible() && rightSection.isManaged();
    }
}


//too hard to implement, or I just have a skill issue :leko_shrug:
class TimeKeepingThread extends Thread {
    private Label statusBarLabel;
    private String statusBarLabeContent;

    public TimeKeepingThread(Label statusBarLabel, String statusBarLabeContent) {
        this.statusBarLabel = statusBarLabel;
        this.statusBarLabeContent = statusBarLabeContent;
    }

    public void run() {
        int minutes = 0;
        boolean stopTheLoop = false;
        while (!stopTheLoop) {

            String timePassed;
            switch (minutes) {
                case 0:
                    timePassed = "(pred nekaj trenutki)";
                    break;
                case 1:
                    timePassed = "(pred 1 minuto)";
                    break;
                case 2:
                    timePassed = "(pred 2 minutama)";
                    break;
                case 5:
                    timePassed = "(danes " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + ")";
                    stopTheLoop = true;
                    break;
                default:
                    timePassed = "(pred " + minutes + " minutami)";
            }
            String finalTimePassed = timePassed;
            Platform.runLater(() -> {
                statusBarLabeContent = statusBarLabeContent.replaceAll("\\s\\(.*\\)$", "");
                statusBarLabeContent = statusBarLabeContent + " " + finalTimePassed;
                statusBarLabel.setText(statusBarLabeContent);
            });
            minutes++;
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



