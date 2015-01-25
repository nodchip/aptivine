package aptivine;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.google.api.client.util.Strings;

public class View {

  private Controller controller;
  private Shell shell;
  private Display display;
  private Text textFolderPath;
  private Button buttonSelectFolderPath;
  private Button buttonReload;
  private Button buttonMarkAllUpgrade;
  private Button buttonApply;
  private Table table;
  private ProgressBar progressBar;
  private Label labelStatusBar;
  private Composite compositeTable;

  public View() {
    display = new Display();
    shell = new Shell(display);
    shell.setText("Aptivine ver " + Constants.VERSION);
    shell.setLayout(new GridLayout(1, true));

    Composite composite = new Composite(shell, SWT.NO_FOCUS);

    {
      Composite composite2 = new Composite(composite, SWT.NO_FOCUS);

      {
        Label label = new Label(composite2, SWT.NONE);
        label.setText("Irvineインストールフォルダ");
      }

      {
        textFolderPath = new Text(composite2, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        textFolderPath.setLayoutData(gridData);
        textFolderPath.setEnabled(false);
      }

      {
        buttonSelectFolderPath = new Button(composite2, SWT.NONE);
        buttonSelectFolderPath.setText("選択");
        buttonSelectFolderPath.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            selectFolderPath();
          }
        });
      }

      GridLayout gridLayout = new GridLayout(3, false);
      composite2.setLayout(gridLayout);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      composite2.setLayoutData(gridData);
    }

    {
      Composite composite2 = new Composite(composite, SWT.NO_FOCUS);
      GridLayout gridLayout = new GridLayout(3, false);
      composite2.setLayout(gridLayout);

      {
        buttonReload = new Button(composite2, SWT.NONE);
        buttonReload.setText("再読み込み");
        buttonReload.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            new Thread(() -> {
              controller.reload();
            }).start();
          }
        });
      }

      {
        buttonMarkAllUpgrade = new Button(composite2, SWT.NONE);
        buttonMarkAllUpgrade.setText("更新されたパッケージを選択");
        buttonMarkAllUpgrade.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            new Thread(() -> {
              controller.markAllUpgrade();
            }).start();
          }
        });
      }

      {
        buttonApply = new Button(composite2, SWT.NONE);
        buttonApply.setText("適用");
        buttonApply.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            new Thread(() -> {
              controller.apply();
            }).start();
          }
        });
      }
    }

    {
      compositeTable = new Composite(composite, SWT.NONE);
      compositeTable.setLayout(new GridLayout(1, true));
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      gridData.verticalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      gridData.grabExcessVerticalSpace = true;
      compositeTable.setLayoutData(gridData);

      recreateTable();
    }

    {
      progressBar = new ProgressBar(composite, SWT.NONE);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      progressBar.setLayoutData(gridData);
    }

    {
      labelStatusBar = new Label(composite, SWT.BORDER);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessHorizontalSpace = true;
      labelStatusBar.setLayoutData(gridData);
    }

    composite.setLayout(new GridLayout(1, true));
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.verticalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    composite.setLayoutData(gridData);
    composite.pack();
  }

  private void recreateTable() {
    if (table != null) {
      table.dispose();
    }

    table = new Table(compositeTable, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
        | SWT.CHECK);
    GridData tableGrid = new GridData();
    tableGrid.horizontalAlignment = GridData.FILL;
    tableGrid.verticalAlignment = GridData.FILL;
    tableGrid.grabExcessHorizontalSpace = true;
    tableGrid.grabExcessVerticalSpace = true;
    table.setLayoutData(tableGrid);
    // 線を表示する
    table.setLinesVisible(true);
    // ヘッダを可視にする
    table.setHeaderVisible(true);
    // 列のヘッダの設定
    TableColumn columnCheck = new TableColumn(table, SWT.LEFT);
    columnCheck.setWidth(32);

    TableColumn columnPackage = new TableColumn(table, SWT.NONE);
    columnPackage.setText("パッケージ");
    columnPackage.setWidth(128);

    TableColumn columnInstalledVersion = new TableColumn(table, SWT.NONE);
    columnInstalledVersion.setText("現在のバージョン");
    columnInstalledVersion.setWidth(64);

    TableColumn columnLatestVersion = new TableColumn(table, SWT.NONE);
    columnLatestVersion.setText("最新バージョン");
    columnLatestVersion.setWidth(64);

    TableColumn columnSize = new TableColumn(table, SWT.NONE);
    columnSize.setText("サイズ");
    columnSize.setWidth(64);

    compositeTable.layout();
  }

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public void start() {
    shell.pack();
    shell.open();

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }

    display.dispose();
  }

  public void setEnabled(boolean enabled) {
    checkAsyncExec(() -> {
      Control[] controls = { buttonSelectFolderPath, buttonReload, buttonMarkAllUpgrade,
          buttonApply, table, progressBar };
      for (Control control : controls) {
        control.setEnabled(enabled);
      }
    });
  }

  public void setUploadedFiles(List<Package> files) {
    checkAsyncExec(() -> {
      recreateTable();

      for (Package file : files) {
        String[] strings = { "", file.getId(), "", file.getVersionAsString(), file.getFileSize() };
        TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(strings);
      }
    });
  }

  public void setStatusBar(String status) {
    checkAsyncExec(() -> {
      labelStatusBar.setText(status);
    });
  }

  private boolean checkAsyncExec(Runnable r) {
    if (!display.isDisposed()) {
      display.asyncExec(r);
      return true;
    } else {
      return false;
    }
  }

  public void selectFolderPath() {
    DirectoryDialog dialog = new DirectoryDialog(shell);
    String folderPath = textFolderPath.getText();
    if (!Strings.isNullOrEmpty(folderPath)) {
      dialog.setFilterPath(folderPath);
    }
    folderPath = dialog.open();
    if (!Strings.isNullOrEmpty(folderPath)) {
      textFolderPath.setText(folderPath);
    }
  }
}
