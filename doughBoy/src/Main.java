import org.dreambot.api.ClientSettings;
import org.dreambot.api.data.ClientLayout;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.input.event.impl.keyboard.awt.Key;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.widgets.message.Message;

import java.awt.*;
import java.text.DecimalFormat;


@ScriptManifest(category = Category.MONEYMAKING, description = "Makes Pizza base", name = "Dough Boy", author = "dwhylin", version = 1.0)

public class Main extends AbstractScript implements ChatListener {

    String flour = "Pot of flour";
    String water = "Bucket of water";
    String dough = "Pizza base";
    String emptyPot = "Pot";
    String emptyBucket = "Bucket";
    String status;
    int notedPot = 1934;
    int notedBucket = 1930;
    int totalMade;
    int profit;
    int profitMargin;
    int gpPerHour;
    int totalGpPerHour;
    int doughPrice;
    int emptyBucketPrice;
    int emptyPotPrice;
    int potOfFlourPrice;
    int bucketOfWaterPrice;
    int threeCount;
    int mouseCount;
    long time;
    int flourInBank;
    int waterInBank;
    String version = "1.0";
    Area geArea = new Area(3161, 3493, 3168, 3486);

    public void onStart() {
        time = System.currentTimeMillis();
        mouseCount = 0;
        threeCount = 0;
        totalMade = 0;
        profit = 0;
        waterInBank = 0;
        flourInBank = 0;
        doughPrice = LivePrices.get(dough);
        emptyBucketPrice = LivePrices.get(emptyBucket);
        emptyPotPrice = LivePrices.get(emptyPot);
        potOfFlourPrice = LivePrices.get(flour);
        bucketOfWaterPrice = LivePrices.get(water);
        profitMargin = (doughPrice + emptyBucketPrice + emptyPotPrice) - (potOfFlourPrice + bucketOfWaterPrice);

    }

    public String getStatus() {
        if (Inventory.contains(flour) && Inventory.contains(water) && !Bank.isOpen() && !GrandExchange.isOpen()) {
            status = "Making Pizza Base";
        }
        if (Inventory.contains(emptyPot) && !Inventory.contains(flour) && !GrandExchange.isOpen() || Bank.isOpen()) {
            status = "Banking";
        }
        if (GrandExchange.isOpen()) {
            status = "Handling Grand Exchange";
        }
        return status;
    }

    public void onMessage(Message message) {
        if (message.getMessage().contains("You mix")) {
            totalMade++;
            profit = totalMade * (doughPrice + emptyBucketPrice + emptyPotPrice - potOfFlourPrice - bucketOfWaterPrice);
        }
    }

    public int totalCoins() {
        return Inventory.count("Coins") + Bank.count("Coins");
    }
    public int maxBuyFlour() {
        if (Inventory.count("Coins") / LivePrices.getHigh(flour) / Calculations.random(3, 4) > 13000) {
            return 13000;
        } else
        return Inventory.count("Coins") / LivePrices.getHigh(flour) / Calculations.random(5, 8);
    }

    public int findWaterBanked() {
        if(Bank.contains(water)) {
            Bank.count(water);
        }
        return Bank.count(water);
    }
    public int findFlourBanked() {
        if(Bank.contains(flour)) {
            Bank.count(flour);
        }
        return Bank.count(flour);
    }

    public boolean differentAmountsTooLarge() {
        if(findFlourBanked() - findWaterBanked() >= 13000) {
            return true;
        }
        return findWaterBanked() - findFlourBanked() >= 13000;
    }
    public int totalCountFlour() {
        return Inventory.count(flour) + findFlourBanked();
    }
    public int totalCountWater() {
        return Inventory.count(water) + findWaterBanked();
    }


    public void handleGE() {
        NPC geClerk = NPCs.closest("Grand Exchange Clerk");
        WidgetChild offerOne = Widgets.get(465, 7, 5);

        if (GrandExchange.isOpen() && offerOne != null) {
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1150);
        }
        if (GrandExchange.isReadyToCollect()) {
            GrandExchange.collect();
            Sleep.sleep(1000);
        }
        if (!Bank.isOpen() && !GrandExchange.isOpen() && Inventory.count(dough) > 28 || !Bank.isOpen() && !GrandExchange.isOpen() && Inventory.contains("Coins")
                && !Inventory.contains(flour) && !Inventory.contains(water) || !Bank.isOpen() && differentAmountsTooLarge() && !GrandExchange.isOpen()) {
            if (Calculations.random(1, 15) >= 11) {
                Logger.log("rotating camera to clerk");
                Camera.rotateToEntity(geClerk);
                Sleep.sleep(450, 1150);
            }
            GrandExchange.open();
            Sleep.sleepUntil(GrandExchange::isOpen, Calculations.random(1550, 3550));
            if (GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
            }
        }
        if (GrandExchange.isOpen() && Inventory.count(dough) > 0 && !GrandExchange.isReadyToCollect()) {
            GrandExchange.sellItem(dough, Inventory.count(dough), LivePrices.getLow(dough) - Calculations.random(40, 55));
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, Calculations.random(1950, 2950));
            GrandExchange.collect();
            Sleep.sleep(1950, 4550);
        }
        if (GrandExchange.isOpen() && Inventory.count(emptyPot) > 0 && !GrandExchange.isReadyToCollect()) {
            GrandExchange.sellItem(emptyPot, Inventory.count(emptyPot), 1);
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, Calculations.random(1950, 2950));
            GrandExchange.collect();
            Sleep.sleep(1950, 4550);

        }

        if (GrandExchange.isOpen() && Inventory.count(emptyBucket) > 0 && !GrandExchange.isReadyToCollect()) {
            GrandExchange.sellItem(emptyBucket, Inventory.count(emptyBucket), 1);
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, Calculations.random(1950, 2950));
            GrandExchange.collect();

        }
        if (Inventory.contains("Coins") && GrandExchange.isOpen() && totalCountFlour() < 10
                && !GrandExchange.isReadyToCollect()) {
            GrandExchange.buyItem(flour, maxBuyFlour(), LivePrices.getHigh("Pot of flour") + Calculations.random(6, 11));
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1950);
            GrandExchange.collect();
            Sleep.sleep(1950, 4550);
        }
        if (Inventory.contains("Coins") && GrandExchange.isOpen() && totalCountFlour() >= 10 && totalCountWater() < 10
                && !Inventory.contains(dough) && !GrandExchange.isReadyToCollect()) {
            GrandExchange.buyItem(water, Inventory.count(flour), LivePrices.getHigh("Bucket of water") + Calculations.random(6, 11));
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1950);
            GrandExchange.collect();
            Sleep.sleep(1950, 4550);
        }
        if (GrandExchange.isOpen() && findWaterBanked() > findFlourBanked() && differentAmountsTooLarge()) {
            int moreWater = findWaterBanked() - findFlourBanked();
            GrandExchange.buyItem(flour, moreWater, LivePrices.getHigh("Pot of flour") + Calculations.random(6, 11));
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1950);
            GrandExchange.collect();
            Sleep.sleep(1950, 4550);
        }
        if (GrandExchange.isOpen() && findWaterBanked() < findFlourBanked() && differentAmountsTooLarge()) {
            int moreFlour = (findFlourBanked() + Inventory.count(flour)) - (findWaterBanked() + Inventory.count(water));
            GrandExchange.buyItem(water, moreFlour, LivePrices.getHigh("Bucket of water") + Calculations.random(6, 11));
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1950);
            GrandExchange.collect();
            Sleep.sleep(1950, 4550);
        }

        if (totalCountWater() >= 10 && totalCountFlour() >= 10 && !GrandExchange.isReadyToCollect()) {
            GrandExchange.close();
        }

    }


    @Override
    public int onLoop() {
        NPC banker = NPCs.closest("Banker");
        WidgetChild pizzaBaseWidget = Widgets.get(270, 16, 38);

        if (!geArea.contains(Players.getLocal())) {
            Walking.walk(geArea);
            Sleep.sleepUntil(() -> geArea.contains(Players.getLocal()), 250);
            return (Calculations.random(1550, 5550));
        }

        if(totalCoins() == 0 && geArea.contains(Players.getLocal())) {
            if (ClientSettings.getClientLayout() != ClientLayout.FIXED_CLASSIC) {
                ClientSettings.setClientLayout(ClientLayout.FIXED_CLASSIC);
            }
            if (ClientSettings.getClientLayout() == ClientLayout.FIXED_CLASSIC) {
                Bank.open();
                return Calculations.random(1050, 3550);
            }
            return Calculations.random(1000, 2550);
        }

        handleGE();

        if (Bank.isOpen() && Bank.count(flour) < 9 && Inventory.count(flour) < 9 || Bank.isOpen() && Bank.count(water) < 9 && Inventory.count(water) < 9) {
            findFlourBanked();
            findWaterBanked();
            if (Bank.getWithdrawMode() != BankMode.NOTE) {
                Bank.setWithdrawMode(BankMode.NOTE);
                Sleep.sleep(450, 850);
            }
            if (Bank.contains(dough)) {
                Bank.withdrawAll(dough);
                Sleep.sleep(650, 950);
            }
            if (Bank.contains(emptyPot)) {
                Bank.withdrawAll(emptyPot);
                Sleep.sleep(650, 950);
            }
            if (Bank.contains(emptyBucket)) {
                Bank.withdrawAll(emptyBucket);
                Sleep.sleep(650, 950);
            }
            if (Bank.contains("Coins")) {
                Bank.withdrawAll("Coins");
                Sleep.sleep(450, 850);
                Bank.close();
                Sleep.sleep(450, 850);
            }
            return Calculations.random(1550, 3950);

        }
        if(Bank.isOpen() && differentAmountsTooLarge() && Inventory.contains("Coins")) {
            Bank.close();
        }

        if (!Bank.isOpen() && banker != null && Inventory.count(dough) == 9 ||
                !Bank.isOpen() && !GrandExchange.isOpen() && Inventory.count(flour) > 9
                || Inventory.contains(notedPot) && Inventory.contains(notedBucket) && !GrandExchange.isOpen()
                || Inventory.isEmpty() && banker != null && !Bank.isOpen()
                || Inventory.count(flour) > 9 && Inventory.contains("Coins")
                || Inventory.count(water) > 9 && Inventory.contains("Coins")
                || Inventory.isFull() || !Inventory.isEmpty() && !Inventory.contains("Coins") && !Inventory.contains(flour)
                && !Inventory.contains(water) && !Inventory.contains(emptyBucket) && !Inventory.contains(emptyPot)) {
            if (!Players.getLocal().isMoving()) {
                if (geArea.contains(Players.getLocal())) {
                    Bank.open();
                    Sleep.sleepUntil(Bank::isOpen, Calculations.random(800, 1250));
                }
            }
        }
        if (Bank.isOpen() && Inventory.contains(emptyPot) || Bank.isOpen() && Inventory.count(flour) > 9
                || Inventory.contains(notedPot) || Inventory.contains(notedBucket)
                || Inventory.count(flour) > 9 && !Inventory.contains("Coins")
                || Inventory.count(water) > 9 && !Inventory.contains("Coins")
                || !Inventory.isEmpty() && !Inventory.contains(flour) && !Inventory.contains(water) && !Inventory.contains("Coins")) {
            Bank.depositAllItems();
            Sleep.sleepUntil(Inventory::isEmpty, 750);
            return Calculations.random(850, 1450);
        }
        if (Bank.isOpen() && Inventory.isEmpty() && !Inventory.contains(flour) && Bank.count(flour) >= 9) {
            if (Bank.getWithdrawMode() != BankMode.ITEM) {
                Bank.setWithdrawMode(BankMode.ITEM);
                Sleep.sleep(750, 1350);
            }
            Bank.withdraw(flour, 9);
            Sleep.sleepUntil(() -> Inventory.count(flour) == 9, 250);
        }
        if (Bank.isOpen() && Inventory.count(flour) == 9 && !Inventory.contains(water) && Bank.count(water) >= 9) {
            Bank.withdraw(water, 9);
            Sleep.sleepUntil(() -> Inventory.count(water) == 9, 250);
        }
        if (Bank.isOpen() && Inventory.count(flour) == 9 && Inventory.count(water) == 9) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(250, 650));
        }
        if (!Bank.isOpen() && !GrandExchange.isOpen() && Inventory.count(flour) >= 1 && Inventory.count(water) >= 1) {
            if (pizzaBaseWidget == null) {
                Inventory.combine(flour, water);
                Sleep.sleepUntil(() -> pizzaBaseWidget != null, Calculations.random(750, 1150));
                return Calculations.random(450, 850);
            }
            if (pizzaBaseWidget != null) {
                if (mouseCount < 1) {
                    mouseCount++;
                    pizzaBaseWidget.interact();
                    Sleep.sleepUntil(() -> Inventory.count(dough) == 9, Calculations.random(750, 8500));

                } else
                Keyboard.typeKey(Key.SPACE);
                Keyboard.releaseKey(Key.SPACE);
                Sleep.sleep(1050, 6450);
                Mouse.move(banker);
                Sleep.sleepUntil(() -> Inventory.count(dough) == 9, Calculations.random(2550, 8500));
            }
            Sleep.sleepUntil(() -> Inventory.count(dough) == 9, Calculations.random(2550, 8500));

        }
        return 0;
    }
    public void onPaint(Graphics2D g) {
        DecimalFormat df = new DecimalFormat("#");
        gpPerHour = (int)(profit / ((System.currentTimeMillis() - time) / 3600000.0D));
        totalGpPerHour = gpPerHour / 1000;


        Color c = new Color(25,58,74, 200);
        g.setColor(c);
        g.fillRect(550, 205, 185, 260);

        g.setColor(Color.MAGENTA);
        g.drawString("Profit margin: " + profitMargin, 560, 450);
        g.drawString("Coins: " + totalCoins(), 560, 410);
        g.drawString("water banked: " + findWaterBanked(), 560, 390);
        g.drawString("flour banked: " + findFlourBanked(), 560, 370);
        g.drawString("Profit/hr: " + df.format(gpPerHour), 560, 350);
        g.drawString("Profit: " + profit, 560, 330);
        g.drawString("Pizza Base made: " + totalMade, 560, 310);
        g.drawString("Status: " + getStatus(), 560, 290);
        g.drawString("Time running: "+ formatTime(System.currentTimeMillis()-time), 560, 260);

        g.setColor(Color.WHITE);
        g.drawString("Dwhylin's Dough Boy v" + version, 575, 220);

    }

    public final String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}