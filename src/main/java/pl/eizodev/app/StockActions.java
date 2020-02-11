package pl.eizodev.app;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

class StockActions {
    private static void timeOfTransaction() {
        Random random = new Random();
        int time = random.nextInt(301);
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean containsStock(final List<Stock> list, final String ticker) {
        return list.stream().anyMatch(o -> o.getTicker().equals(ticker));
    }

    private static void removeStockFromList(final List<Stock> list, final String ticker) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker) && o.getQuantity() == 0)
                .forEach(list::remove);
    }

    private static void settingStockQuantity(final List<Stock> list, String ticker, int quantity, int ratio) {
        list.stream()
                .filter(o -> o.getTicker().equals(ticker))
                .forEach(o -> o.setQuantity(o.getQuantity() + (ratio * quantity)));
    }

    private static void settingParamsOfWallet(User user, int quantity, Stock stockWIG20, int ratio) {
        if (ratio == -1) {
            user.setStockValue(0);
        } else {
            user.setStockValue(user.getStockValue() + (ratio * (quantity * stockWIG20.getPrice())));
        }
        user.setBalanceAvailable(user.getBalanceAvailable() - (ratio * (quantity * stockWIG20.getPrice())));
        user.setWalletValue(user.getStockValue() + user.getBalanceAvailable());
    }

    private static void stockPurchaseParameters(int quantity, List<Stock> userStock, String ticker, Stock stockWIG20, User user) {
//        Dodanie akcji do konta uzytkownika, gdy uzytkownik posiada akcje ktore chce kupic:
        if (containsStock(userStock, ticker)) {
            userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .forEach(o -> o.setAveragePurchasePrice(((o.getQuantity() * o.getPrice()) + (quantity * stockWIG20.getPrice())) / (o.getQuantity() + quantity)));
            settingStockQuantity(userStock, ticker, quantity, 1);
        } else { // Gdy nie posiada akcji:
            userStock.add(stockWIG20);
            stockWIG20.setQuantity(quantity);
            stockWIG20.setAveragePurchasePrice(stockWIG20.getPrice());
            user.setUserStock(userStock);
        }
        StockWIG20 stock = new StockWIG20();
        settingParamsOfWallet(user, quantity, stock.getByTicker(ticker), 1);
    }

    //Usuwanie sprzedanych akcji z konta uzytkownika
    private static void stockSellParameters(int quantity, int amountOfStock, List<Stock> userStock, User user, String ticker) {
        if (quantity <= amountOfStock && quantity > 0) {
            settingStockQuantity(userStock, ticker, quantity, -1);
            StockWIG20 stock = new StockWIG20();
            settingParamsOfWallet(user, quantity, stock.getByTicker(ticker), -1);
            removeStockFromList(userStock, ticker);
            user.setUserStock(userStock);
            user.serializeUser(user);
            System.out.println("Transakcja przebiegla pomyslnie.");
        } else {
            System.out.println("Nie posiadasz takiej ilosci akcji! Ilosc akcji w Twoim portfelu: " + amountOfStock);
        }
    }

    void stockPurchase(int quantity, String ticker) {
        User user = new User();
        User finalUser = user.deserializeUser();
        List<Stock> userStock = new CopyOnWriteArrayList<>(finalUser.getUserStock());
        StockWIG20 stock = new StockWIG20();

        if (finalUser.getBalanceAvailable() >= quantity * stock.getByTicker(ticker).getPrice() && quantity > 0) {
            timeOfTransaction();
            stockPurchaseParameters(quantity, userStock, ticker, stock.getByTicker(ticker), finalUser);
            System.out.println("Transakcja przebiegla pomyslnie.");
            user.serializeUser(finalUser);
        } else {
            int maxAmount = (int) Math.floor((finalUser.getBalanceAvailable() / (stock.getByTicker(ticker).getPrice())));
            System.out.println("Nie masz odpowiednich srodkow, maksymalna ilosc akcji jakie mozesz kupic: " + maxAmount);
        }
    }

    void stockSell(int quantity, String ticker) {
        User finalUser = new User();
        finalUser = finalUser.deserializeUser();
        List<Stock> userStock = new CopyOnWriteArrayList<>(finalUser.getUserStock());

        if (containsStock(userStock, ticker)) {
            timeOfTransaction();

            Optional<Integer> amountOfStock = userStock.stream()
                    .filter(o -> o.getTicker().equals(ticker))
                    .map(Stock::getQuantity)
                    .findFirst();

            stockSellParameters(quantity, amountOfStock.get(), userStock, finalUser, ticker);
        } else {
            System.out.println("Nie posiadasz tych akcji!");
        }
    }
}