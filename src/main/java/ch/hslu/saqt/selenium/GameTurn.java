package ch.hslu.saqt.selenium;

/**
 * Representation of one Turn of the Philosophers Game.
 */
public final class GameTurn {
    private final String startPageUrl;
    private final String endPageTitle;
    private final int maxClicks;
    private int clicksNeeded;
    private boolean loopDetected;
    private boolean errorDetected;

    public GameTurn(String startPageUrl, int maxClicks) {
        this.startPageUrl = startPageUrl;
        this.maxClicks = maxClicks;
        this.endPageTitle = "Philosophie – Wikipedia";
        this.clicksNeeded = 0;
        this.loopDetected = false;
    }

    public int getClicksNeeded() {
        return clicksNeeded;
    }

    public void increaseClicksNeeded() {
        this.clicksNeeded++;
    }

    public String getStartPageUrl() {
        return startPageUrl;
    }

    public String getEndPageTitle() {
        return endPageTitle;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public void setLoopDetected() {
        this.loopDetected = true;
    }

    public void setErrorDetected() {
        this.errorDetected = true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Startpage: ").append(this.startPageUrl).append(",").append(System.lineSeparator())
                .append("Max Clicks ").append(this.maxClicks).append(",").append(System.lineSeparator())
                .append("Score: ");

        if (loopDetected) {
            stringBuilder.append("This Startpage reached a Loop after ").append(clicksNeeded)
                    .append(" Clicks.").append(",").append(System.lineSeparator());
        } else if (errorDetected) {
            stringBuilder.append("This Startpage reached a Error after ").append(clicksNeeded)
                    .append(" Clicks.").append(",").append(System.lineSeparator());
        } else {
            if (clicksNeeded >= maxClicks) {
                stringBuilder.append("Endpage not reached with ").append(maxClicks)
                        .append(" Clicks").append(",").append(System.lineSeparator());
            } else {
                stringBuilder.append(this.clicksNeeded).append(" Clicks needed").append(",")
                        .append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }
}
