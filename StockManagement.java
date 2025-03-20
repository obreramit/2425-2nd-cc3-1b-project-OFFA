import java.util.*;

class StockItem {
    String name;
    int quantity;
    int sold;

    public StockItem(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.sold = 0;
    }

    public void sell(int amount) {
        sold += amount;
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }

    public String toString() {
        return name + " - Quantity: " + quantity + ", Sold: " + sold;
    }
}

class User {
    String username, password, role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}

public class StockManagement {
    static Scanner scanner = new Scanner(System.in);
    static Map<String, StockItem> stock = new HashMap<>();
    static List<User> users = new ArrayList<>();
    static User loggedInUser = null;

    public static void main(String[] args) {
        setupDefaultUsers();
        login();
    }

    static void setupDefaultUsers() {
        users.add(new User("manager", "manager123", "Manager"));
        users.add(new User("worker", "worker123", "Worker"));
    }

    static void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.username.equals(username) && user.password.equals(password)) {
                loggedInUser = user;
                System.out.println("Login successful! Role: " + user.role);
                showMenu();
                return;
            }
        }
        System.out.println("Invalid login. Try again.");
        login();
    }

    static void showMenu() {
        while (true) {
            System.out.println("\nAvailable Options:");

            List<String> options = new ArrayList<>();
            List<Runnable> actions = new ArrayList<>();

            if (loggedInUser.role.equals("Manager")) {
                options.add("Add Item");
                actions.add(() -> addItem());
                options.add("Remove Item");
                actions.add(() -> removeItem());
                options.add("Edit Stock");
                actions.add(() -> editStock());
            }

            options.add("View Stock");
            actions.add(() -> viewStock());
            options.add("Record Sale");
            actions.add(() -> recordSale());
            options.add("Best Sellers Report");
            actions.add(() -> bestSellersReport());
            options.add("Logout");
            actions.add(() -> {
                System.out.println("Logging out...");
                login();
            });

            for (int i = 0; i < options.size(); i++) {
                System.out.println((i + 1) + ". " + options.get(i));
            }

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= actions.size()) {
                actions.get(choice - 1).run();
            } else {
                System.out.println("Invalid choice! Try again.");
            }
        }
    }

    static void addItem() {
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        stock.put(name, new StockItem(name, quantity));
        System.out.println("Item added.");
    }

    static void removeItem() {
        System.out.print("Enter item name to remove: ");
        String name = scanner.nextLine();
        if (stock.remove(name) != null) {
            System.out.println("Item removed.");
        } else {
            System.out.println("Item not found.");
        }
    }

    static void editStock() {
        System.out.print("Enter item name to edit: ");
        String name = scanner.nextLine();
        if (!stock.containsKey(name)) {
            System.out.println("Item not found.");
            return;
        }
        System.out.print("Enter new quantity: ");
        int newQuantity = scanner.nextInt();
        stock.get(name).updateQuantity(newQuantity);
        System.out.println("Stock updated.");
    }

    static void viewStock() {
        if (stock.isEmpty()) {
            System.out.println("Stock is empty.");
            return;
        }
        stock.values().forEach(System.out::println);
    }

    static void recordSale() {
        if (stock.isEmpty()) {
            System.out.println("No items available for sale.");
            return;
        }

        System.out.println("Available items:");
        int index = 1;
        List<String> itemNames = new ArrayList<>(stock.keySet());
        for (String item : itemNames) {
            System.out.println(index + ". " + stock.get(item));
            index++;
        }

        System.out.print("Enter the item number to record sale: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > itemNames.size()) {
            System.out.println("Invalid choice.");
            return;
        }

        String selectedItem = itemNames.get(choice - 1);
        System.out.print("Enter quantity sold: ");
        int amount = scanner.nextInt();
        stock.get(selectedItem).sell(amount);
        System.out.println("Sale recorded for " + selectedItem + ".");
    }

    static void bestSellersReport() {
        System.out.println("\nBest Selling Items:");
        stock.values().stream()
                .sorted((a, b) -> Integer.compare(b.sold, a.sold))
                .limit(3)
                .forEach(System.out::println);
    }
}
