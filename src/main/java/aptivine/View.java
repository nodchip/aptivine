package aptivine;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.google.inject.Inject;

public class View {

  private static final int COLUMN_ID = 1;
  private static final int COLUMN_INSTALLED_VERSION = 2;
  private static final int COLUMN_LATEST_VERSION = 3;

  private final PackageUtils packageUtils;
  private Controller controller;
  private Shell shell;
  private Display display;
  private Text textIrvineFolderPath;
  private Button buttonSelectFolderPath;
  private Button buttonReload;
  private Button buttonMarkAllUpgrade;
  private Button buttonApply;
  private Table table;
  private ProgressBar progressBar;
  private Label labelStatusBar;
  private Composite compositeTable;

  @Inject
  public View(PackageUtils packageUtils) {
    this.packageUtils = checkNotNull(packageUtils);

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
        textIrvineFolderPath = new Text(composite2, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        textIrvineFolderPath.setLayoutData(gridData);
        textIrvineFolderPath.setEnabled(false);
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
            String irvineFolderPath = getIrvineFolderPath();
            List<String> markedPackageIds = getMarkedPackageIds();
            new Thread(() -> {
              controller.apply(irvineFolderPath, markedPackageIds);
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

  public void setUploadedFiles(List<Package> files, Map<String, Package> installedPackages) {
    checkAsyncExec(() -> {
      recreateTable();

      for (Package file : files) {
        String id = file.getId();
        String installedVersion;
        if (installedPackages.containsKey(id)) {
          installedVersion = packageUtils.getVersionAsString(installedPackages.get(id)
              .getFileName());
        } else {
          installedVersion = "";
        }
        String latestVersion = packageUtils.getVersionAsString(file.getFileName());
        String fileSize = file.getFileSize();
        String[] strings = { "", id, installedVersion, latestVersion, fileSize };
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

  public void setProgress(int min, int max, int selection) {
    checkAsyncExec(() -> {
      progressBar.setMinimum(min);
      progressBar.setMaximum(max);
      progressBar.setSelection(selection);
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
    String folderPath = textIrvineFolderPath.getText();
    if (!Strings.isNullOrEmpty(folderPath)) {
      dialog.setFilterPath(folderPath);
    }
    folderPath = dialog.open();
    if (!Strings.isNullOrEmpty(folderPath)) {
      textIrvineFolderPath.setText(folderPath);
    }
  }

  public void markAllUpgrade() {
    checkAsyncExec(() -> {
      int itemCount = table.getItemCount();
      for (int itemIndex = 0; itemIndex < itemCount; ++itemIndex) {
        TableItem item = table.getItem(itemIndex);
        String installedVersion = item.getText(COLUMN_INSTALLED_VERSION);
        if (Strings.isNullOrEmpty(installedVersion)) {
          continue;
        }

        String latestVersion = item.getText(COLUMN_LATEST_VERSION);
        if (packageUtils.toDouble(installedVersion) >= packageUtils.toDouble(latestVersion)) {
          continue;
        }

        item.setChecked(true);
      }
    });
  }

  private List<String> getMarkedPackageIds() {
    List<String> ids = new ArrayList<>();
    int itemCount = table.getItemCount();
    for (int itemIndex = 0; itemIndex < itemCount; ++itemIndex) {
      TableItem item = table.getItem(itemIndex);
      if (!item.getChecked()) {
        continue;
      }
      String id = item.getText(COLUMN_ID);
      ids.add(id);
    }
    return ids;
  }

  private String getIrvineFolderPath() {
    return textIrvineFolderPath.getText();
  }
}
