import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;
import java.util.Calendar;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import javax.swing.RowSorter;

class SaleRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String itemName;
    private final int quantity;
    private final double price;
    private final Date saleDate;

    public SaleRecord(String itemName, int quantity, double price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.saleDate = new Date();
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public Date getSaleDate() {
        return saleDate;
    }

    public double getTotalAmount() {
        return quantity * price;
    }
}

class StockItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private int quantity;
    private int sold;
    private double price;
    private int minimumStock;
    private String category;
    private String lastUpdated;
    private final List<SaleRecord> salesHistory = new ArrayList<>();

    public StockItem(String name, int quantity, double price, int minimumStock, String category) {
        this.name = name;
        this.quantity = Math.max(quantity, 0);
        this.sold = 0;
        this.price = price;
        this.minimumStock = minimumStock;
        this.category = category;
        this.lastUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getSold() {
        return sold;
    }

    public double getPrice() {
        return price;
    }

    public int getMinimumStock() {
        return minimumStock;
    }

    public String getCategory() {
        return category;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public boolean isLowStock() {
        return quantity <= minimumStock;
    }

    public void sell(int amount) {
        if (amount > 0 && amount <= quantity) {
            sold += amount;
            quantity -= amount;
            salesHistory.add(new SaleRecord(name, amount, price));
        } else {
            JOptionPane.showMessageDialog(null, "Not enough stock available.");
        }
    }

    public void addStock(int amount) {
        if (amount > 0) {
            quantity += amount;
        } else {
            JOptionPane.showMessageDialog(null, "Invalid quantity. Must be greater than zero.");
        }
    }

    public void setPrice(double price) {
        this.price = price;
        this.lastUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void setMinimumStock(int minimumStock) {
        this.minimumStock = minimumStock;
        this.lastUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public void setCategory(String category) {
        this.category = category;
        this.lastUpdated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public List<SaleRecord> getSalesHistory() {
        return salesHistory;
    }

    public double getTotalSales() {
        return salesHistory.stream()
                .mapToDouble(SaleRecord::getTotalAmount)
                .sum();
    }

    public int getTotalSold() {
        return salesHistory.stream()
                .mapToInt(SaleRecord::getQuantity)
                .sum();
    }

    @Override
    public String toString() {
        return String.format("%s - Quantity: %d, Sold: %d, Price: $%.2f, Category: %s, Last Updated: %s",
                name, quantity, sold, price, category, lastUpdated);
    }
}

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String username;
    private final String password;
    private final String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String inputPassword) {
        return password.equals(inputPassword);
    }

    public String getRole() {
        return role;
    }
}

public class StockManagementGUI {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTable stockTable;
    private JButton addStockButton, sellStockButton, searchButton, filterButton, exportButton, salesReportButton;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private final Map<String, StockItem> stock = new HashMap<>();
    private final List<User> users = new ArrayList<>();
    private User loggedInUser = null;

    public StockManagementGUI() {
        initializeUsers();
        initializeStockData();
        initializeGUI();
        setupLookAndFeel();
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeUsers() {
        users.clear();
        users.add(new User("manager", "manager123", "Manager"));
        users.add(new User("worker", "worker123", "Worker"));
        System.out.println("Created default users");
    }

    private void initializeStockData() {
        // Create some sample stock items
        stock.clear();
        stock.put("Laptop", new StockItem("Laptop", 10, 999.99, 2, "Electronics"));
        stock.put("Smartphone", new StockItem("Smartphone", 15, 699.99, 3, "Electronics"));
        stock.put("T-Shirt", new StockItem("T-Shirt", 50, 19.99, 10, "Clothing"));
        stock.put("Jeans", new StockItem("Jeans", 30, 49.99, 5, "Clothing"));
        stock.put("Coffee", new StockItem("Coffee", 100, 9.99, 20, "Food"));
        stock.put("Bread", new StockItem("Bread", 40, 3.99, 15, "Food"));
        System.out.println("Created sample stock data with " + stock.size() + " items");
    }

    private void validateStockData() {
        List<String> invalidItems = new ArrayList<>();
        for (Map.Entry<String, StockItem> entry : stock.entrySet()) {
            StockItem item = entry.getValue();
            if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
                invalidItems.add(entry.getKey());
            }
        }

        if (!invalidItems.isEmpty()) {
            System.out.println("Found " + invalidItems.size() + " invalid items in stock data");
            for (String itemName : invalidItems) {
                stock.remove(itemName);
            }
        }
    }

    private void initializeGUI() {
        frame = new JFrame("Stock Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        setupLoginPanel();
        setupDashboardPanel();

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Stock Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 5, 20, 5);
        loginPanel.add(titleLabel, gbc);

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(200, 30));

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = 1;
        loginPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginPanel.add(loginButton, gbc);

        mainPanel.add(loginPanel, "Login");

        loginButton.addActionListener(e -> authenticateUser());
    }

    private void setupDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout(10, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with search and filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        categoryFilter = new JComboBox<>(new String[] { "All", "Electronics", "Clothing", "Food", "Other" });
        filterButton = new JButton("Filter");

        // Add input validation for search field
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                validateSearch();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                validateSearch();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                validateSearch();
            }

            private void validateSearch() {
                String text = searchField.getText();
                if (text.length() > 50) {
                    searchField.setText(text.substring(0, 50));
                }
            }
        });

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(new JLabel("Category:"));
        topPanel.add(categoryFilter);
        topPanel.add(filterButton);

        // Stock display with table
        String[] columns = { "Name", "Quantity", "Price", "Category", "Last Updated" };
        Object[][] data = {};
        stockTable = new JTable(new StockTableModel(data, columns));
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.getTableHeader().setReorderingAllowed(false);
        stockTable.setSelectionBackground(new Color(230, 240, 255));
        stockTable.setSelectionForeground(Color.BLACK);

        // Enable sorting
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(stockTable.getModel());
        stockTable.setRowSorter(sorter);
        sorter.toggleSortOrder(0); // Toggle to ascending order

        // Add error handling for table operations
        stockTable.getModel().addTableModelListener(e -> {
            try {
                validateStockData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error validating stock data: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setPreferredSize(new Dimension(700, 400));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addStockButton = new JButton("Add Stock");
        sellStockButton = new JButton("Sell Stock");
        exportButton = new JButton("Export Data");
        salesReportButton = new JButton("Sales Report");
        JButton logoutButton = new JButton("Logout");

        // Initially disable all buttons
        addStockButton.setEnabled(false);
        sellStockButton.setEnabled(false);
        exportButton.setEnabled(false);
        salesReportButton.setEnabled(false);

        buttonPanel.add(addStockButton);
        buttonPanel.add(sellStockButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(salesReportButton);
        buttonPanel.add(logoutButton);

        // Add table selection listener
        stockTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                sellStockButton.setEnabled(stockTable.getSelectedRow() != -1 &&
                        loggedInUser != null &&
                        ("Worker".equals(loggedInUser.getRole()) || "Manager".equals(loggedInUser.getRole())));
            }
        });

        // Add components to dashboard
        dashboardPanel.add(topPanel, BorderLayout.NORTH);
        dashboardPanel.add(scrollPane, BorderLayout.CENTER);
        dashboardPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(dashboardPanel, "Dashboard");

        // Add action listeners
        addStockButton.addActionListener(e -> handleStockAddition());
        sellStockButton.addActionListener(e -> handleStockSale());
        exportButton.addActionListener(e -> exportStockData());
        searchButton.addActionListener(e -> searchStock());
        filterButton.addActionListener(e -> filterStock());
        salesReportButton.addActionListener(e -> showSalesReport());
        logoutButton.addActionListener(e -> logout());

        // Initial refresh
        refreshStockDisplay();
    }

    private void searchStock() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        refreshStockDisplay(searchTerm);
    }

    private void filterStock() {
        String category = (String) categoryFilter.getSelectedItem();
        if (category != null && !"All".equals(category)) {
            refreshStockDisplay("", category);
        } else {
            refreshStockDisplay();
        }
    }

    private void refreshStockDisplay() {
        refreshStockDisplay("", "All");
    }

    private void refreshStockDisplay(String searchTerm) {
        refreshStockDisplay(searchTerm, "All");
    }

    private void refreshStockDisplay(String searchTerm, String category) {
        List<StockItem> filteredItems = new ArrayList<>();
        for (StockItem item : stock.values()) {
            if ((searchTerm.isEmpty() || item.getName().toLowerCase().contains(searchTerm)) &&
                    (category == null || "All".equals(category) || item.getCategory().equals(category))) {
                filteredItems.add(item);
            }
        }

        Object[][] data = new Object[filteredItems.size()][5];
        for (int i = 0; i < filteredItems.size(); i++) {
            StockItem item = filteredItems.get(i);
            data[i] = new Object[] {
                    item.getName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getCategory(),
                    item.getLastUpdated()
            };
        }

        // Create new model and set it
        StockTableModel model = new StockTableModel(data,
                new String[] { "Name", "Quantity", "Price", "Category", "Last Updated" });
        stockTable.setModel(model);

        // Re-enable sorting
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        stockTable.setRowSorter(sorter);

        // Set column renderers
        stockTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Number) {
                    value = String.format("$%.2f", (Number) value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
    }

    private void exportStockData() {
        if (!"Manager".equals(loggedInUser.getRole())) {
            JOptionPane.showMessageDialog(frame, "Only managers can export data.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("stock_report.csv"))) {
            writer.println("Name,Quantity,Price,Category,Last Updated,Total Sold,Total Sales");
            for (StockItem item : stock.values()) {
                writer.printf("%s,%d,%.2f,%s,%s,%d,%.2f%n",
                        item.getName(), item.getQuantity(), item.getPrice(),
                        item.getCategory(), item.getLastUpdated(),
                        item.getTotalSold(), item.getTotalSales());
            }
            JOptionPane.showMessageDialog(frame, "Stock data exported to stock_report.csv");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error exporting data: " + e.getMessage());
        }
    }

    private void showSalesReport() {
        if (!"Manager".equals(loggedInUser.getRole())) {
            JOptionPane.showMessageDialog(frame, "Only managers can view sales reports.");
            return;
        }

        JDialog dialog = new JDialog(frame, "Sales Report", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 400);

        // Date range panel
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JButton generateButton = new JButton("Generate Report");

        // Set default dates (last 30 days)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow
        String defaultEndDate = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -30); // 30 days before tomorrow
        String defaultStartDate = dateFormat.format(calendar.getTime());

        startDateField.setText(defaultStartDate);
        endDateField.setText(defaultEndDate);

        datePanel.add(new JLabel("Start Date (yyyy-MM-dd):"));
        datePanel.add(startDateField);
        datePanel.add(new JLabel("End Date (yyyy-MM-dd):"));
        datePanel.add(endDateField);
        datePanel.add(generateButton);

        // Report display area
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);

        // Add components to dialog
        dialog.add(datePanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        generateButton.addActionListener(e -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = sdf.parse(startDateField.getText());

                // Create end date with time set to end of day
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(sdf.parse(endDateField.getText()));
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.SECOND, 59);
                final Date endDate = endCal.getTime();

                // Collect all sales records within the date range
                List<SaleRecord> periodSales = new ArrayList<>();
                for (StockItem item : stock.values()) {
                    periodSales.addAll(item.getSalesHistory().stream()
                            .filter(sale -> !sale.getSaleDate().before(startDate) &&
                                    !sale.getSaleDate().after(endDate))
                            .collect(Collectors.toList()));
                }

                // Group sales by item and calculate totals
                Map<String, Integer> quantityByItem = new HashMap<>();
                Map<String, Double> revenueByItem = new HashMap<>();

                for (SaleRecord sale : periodSales) {
                    quantityByItem.merge(sale.getItemName(), sale.getQuantity(), Integer::sum);
                    revenueByItem.merge(sale.getItemName(), sale.getTotalAmount(), Double::sum);
                }

                // Generate report
                StringBuilder report = new StringBuilder();
                report.append("Sales Report for Period: ")
                        .append(sdf.format(startDate))
                        .append(" to ")
                        .append(sdf.format(endDate))
                        .append("\n\n");

                report.append("Most Sold Items:\n");
                report.append("----------------------------------------\n");
                quantityByItem.entrySet().stream()
                        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                        .limit(5)
                        .forEach(entry -> {
                            report.append(String.format("%s: %d units, Revenue: $%.2f\n",
                                    entry.getKey(),
                                    entry.getValue(),
                                    revenueByItem.get(entry.getKey())));
                        });

                report.append("\nTotal Sales Summary:\n");
                report.append("----------------------------------------\n");
                report.append(String.format("Total Items Sold: %d\n",
                        quantityByItem.values().stream().mapToInt(Integer::intValue).sum()));
                report.append(String.format("Total Revenue: $%.2f\n",
                        revenueByItem.values().stream().mapToDouble(Double::doubleValue).sum()));

                reportArea.setText(report.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid dates in the format yyyy-MM-dd");
            }
        });

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void authenticateUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        loggedInUser = users.stream()
                .filter(user -> user.getUsername().equals(username) && user.checkPassword(password))
                .findFirst()
                .orElse(null);

        if (loggedInUser != null) {
            JOptionPane.showMessageDialog(frame, "Login successful! Role: " + loggedInUser.getRole());

            // Enable/disable buttons based on role
            addStockButton.setEnabled("Manager".equals(loggedInUser.getRole()));
            sellStockButton.setEnabled(false); // Initially disabled until item is selected
            exportButton.setEnabled("Manager".equals(loggedInUser.getRole()));
            salesReportButton.setEnabled("Manager".equals(loggedInUser.getRole()));

            cardLayout.show(mainPanel, "Dashboard");
            refreshStockDisplay();
        } else {
            JOptionPane.showMessageDialog(frame, "Invalid login. Try again.");
        }
    }

    private void handleStockAddition() {
        if (!"Manager".equals(loggedInUser.getRole())) {
            JOptionPane.showMessageDialog(frame, "Only managers can add stock.");
            return;
        }

        JDialog dialog = new JDialog(frame, "Add Stock", true);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));

        JTextField nameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField minStockField = new JTextField();
        JComboBox<String> categoryCombo = new JComboBox<>(new String[] { "Electronics", "Clothing", "Food", "Other" });

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Quantity:"));
        dialog.add(quantityField);
        dialog.add(new JLabel("Price:"));
        dialog.add(priceField);
        dialog.add(new JLabel("Minimum Stock:"));
        dialog.add(minStockField);
        dialog.add(new JLabel("Category:"));
        dialog.add(categoryCombo);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());
                int minStock = Integer.parseInt(minStockField.getText());
                String category = (String) categoryCombo.getSelectedItem();

                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Name cannot be empty");
                }

                if (stock.containsKey(name)) {
                    throw new IllegalArgumentException("An item with this name already exists");
                }

                StockItem item = new StockItem(name, quantity, price, minStock, category);
                stock.put(name, item);
                refreshStockDisplay();
                dialog.dispose();
                JOptionPane.showMessageDialog(frame, "Stock added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter valid numbers for quantity, price, and minimum stock.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        dialog.add(submitButton);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void handleStockSale() {
        if (!("Worker".equals(loggedInUser.getRole()) || "Manager".equals(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(frame, "Only workers and managers can sell stock.");
            return;
        }

        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an item to sell.");
            return;
        }

        String itemName = (String) stockTable.getValueAt(selectedRow, 0);
        StockItem item = stock.get(itemName);

        JDialog dialog = new JDialog(frame, "Sell Stock", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);

        // Main panel for sale details
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Item details panel
        JPanel detailsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));

        detailsPanel.add(new JLabel("Item Name:"));
        detailsPanel.add(new JLabel(itemName));
        detailsPanel.add(new JLabel("Available Quantity:"));
        detailsPanel.add(new JLabel(String.valueOf(item.getQuantity())));
        detailsPanel.add(new JLabel("Price per Unit:"));
        detailsPanel.add(new JLabel(String.format("$%.2f", item.getPrice())));

        // Sale input panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Sale Details"));

        JTextField quantityField = new JTextField();
        JLabel totalLabel = new JLabel("Total: $0.00");

        inputPanel.add(new JLabel("Quantity to sell:"));
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel("Total Amount:"));
        inputPanel.add(totalLabel);

        // Add real-time total calculation
        quantityField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateTotal();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateTotal();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateTotal();
            }

            private void updateTotal() {
                try {
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity > 0) {
                        double total = quantity * item.getPrice();
                        totalLabel.setText(String.format("Total: $%.2f", total));
                    } else {
                        totalLabel.setText("Total: $0.00");
                    }
                } catch (NumberFormatException ex) {
                    totalLabel.setText("Total: $0.00");
                }
            }
        });

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Complete Sale");
        JButton cancelButton = new JButton("Cancel");

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        // Add components to dialog
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(detailsPanel, gbc);

        gbc.gridy = 1;
        mainPanel.add(inputPanel, gbc);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        submitButton.addActionListener(e -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    throw new NumberFormatException("Quantity must be greater than zero");
                }
                if (quantity > item.getQuantity()) {
                    throw new IllegalArgumentException("Not enough stock available");
                }

                item.sell(quantity);
                validateStockData();
                refreshStockDisplay();
                dialog.dispose();
                JOptionPane.showMessageDialog(frame,
                        String.format("Successfully sold %d units of %s for $%.2f",
                                quantity, itemName, quantity * item.getPrice()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid quantity.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void logout() {
        loggedInUser = null;
        usernameField.setText("");
        passwordField.setText("");

        // Disable all buttons on logout
        addStockButton.setEnabled(false);
        sellStockButton.setEnabled(false);
        exportButton.setEnabled(false);
        salesReportButton.setEnabled(false);

        cardLayout.show(mainPanel, "Login");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StockManagementGUI::new);
    }
}

class StockTableModel extends javax.swing.table.AbstractTableModel {
    private final String[] columns;
    private final Object[][] data;

    public StockTableModel(Object[][] data, String[] columns) {
        this.data = data;
        this.columns = columns;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 1: // Quantity
                return Integer.class;
            case 2: // Price
                return Double.class;
            default:
                return String.class;
        }
    }
}
