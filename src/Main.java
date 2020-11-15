import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        new Game();
    }
}

class Card {
    private BufferedImage image;
    private String suit;
    private int number;
    private boolean back;

    public Card(BufferedImage image, String suit, int number) {
        this.image = image;
        this.suit = suit;
        this.number = number;
        back = false;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getNumber() {
        return number;
    }

    public void flipCard() {
        back = !back;
    }

    public boolean isFlipped() {
        return back;
    }

    public Card clone() {
        return new Card(image, suit, number);
    }
}

class Game implements ActionListener {
    private final String[] suits = {
            "Clubs",
            "Diamonds",
            "Hearts",
            "Spades"
    };

    private JFrame frame;

    private JPanel panel;
    private JPanel dealerCardsPanel;
    private JPanel playerCardsPanel;

    private JButton hit;
    private JButton stand;

    private BufferedImage back;
    private HashSet<Card> playerCards;
    private HashSet<Card> dealerCards;

    private final ArrayList<Card> cards;
    private ArrayList<Card> deck;

    public Game() {
        frame = new JFrame("Blackjack");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        hit = new JButton("Hit");
        stand = new JButton("Stand");

        hit.addActionListener(this);
        stand.addActionListener(this);

        playerCards = new HashSet();
        dealerCards = new HashSet();
        cards = new ArrayList();
        deck = new ArrayList();

        try {
            back = ImageIO.read(new File("res/Cards/cardBack_blue4.png"));
            for (String suit : suits) {
                for (int number = 2; number <= 10; ++number) {
                    cards.add(new Card(ImageIO.read(new File("res/Cards/card" + suit + number + ".png")), suit, number));
                }

                cards.add(new Card(ImageIO.read(new File("res/Cards/card" + suit + "A.png")), suit, 1));
                cards.add(new Card(ImageIO.read(new File("res/Cards/card" + suit + "J.png")), suit, 10));
                cards.add(new Card(ImageIO.read(new File("res/Cards/card" + suit + "Q.png")), suit, 10));
                cards.add(new Card(ImageIO.read(new File("res/Cards/card" + suit + "K.png")), suit, 10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        panel = new JPanel();

        dealerCardsPanel = new JPanel();
        dealerCardsPanel.setLayout(new FlowLayout());

        playerCardsPanel = new JPanel();
        playerCardsPanel.setLayout(new FlowLayout());

        frame.add(dealerCardsPanel, BorderLayout.NORTH);
        frame.add(playerCardsPanel, BorderLayout.CENTER);
        panel.add(hit, BorderLayout.SOUTH);
        panel.add(stand, BorderLayout.SOUTH);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);

        deal();
        display();
    }

    private void display() {
        dealerCardsPanel.removeAll();
        playerCardsPanel.removeAll();

        for (Card card : dealerCards) {
            if (card.isFlipped()) {
                dealerCardsPanel.add(new JLabel(new ImageIcon(back)));
            } else {
                dealerCardsPanel.add(new JLabel(new ImageIcon(card.getImage())));
            }
        }

        for (Card card : playerCards) {
            playerCardsPanel.add(new JLabel(new ImageIcon(card.getImage())));
        }

        revalidate();
    }

    private void revalidate() {
        frame.invalidate();
        frame.validate();
    }

    private void deal() {
        deck.clear();
        for (Card card : cards) {
            deck.add(card.clone());
        }

        playerCards.clear();
        dealerCards.clear();
        playerCards.add(draw());
        playerCards.add(draw());
        dealerCards.add(draw());
        Card card = draw();
        card.flipCard();
        dealerCards.add(card);
    }

    private Card draw() {
        Card card = deck.get((int) (Math.random() * deck.size()));
        deck.remove(card);
        return card;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(hit)) {
            Card card = draw();
            playerCards.add(card);
            playerCardsPanel.add(new JLabel(new ImageIcon(card.getImage())));
            revalidate();
            check();
        } else if (e.getSource().equals(stand)) {
            dealer();
        }
    }

    public void restart(String result) {
        JOptionPane.showMessageDialog(null, "You have " + result + "!", "", JOptionPane.INFORMATION_MESSAGE);
        deal();
        display();
        frame.repaint();
    }

    public int getValue(Set<Card> cards) {
        int value = 0;
        int aces = 0;

        for (Card card : cards) {
            if (card.getNumber() == 1) {
                ++aces;
            }

            value += card.getNumber();
        }

        for (int i = 0; i < aces; ++i) {
            value += 10;
        }

        while (value > 21 && aces > 0) {
            value -= 10;
            --aces;
        }

        return value;
    }

    public void dealer() {
        dealerCardsPanel.removeAll();

        for (Card card : dealerCards) {
            dealerCardsPanel.add(new JLabel(new ImageIcon(card.getImage())));
        }

        int playerValue = getValue(playerCards);

        while (getValue(dealerCards) < 17) {
            Card card = draw();
            dealerCards.add(card);
            dealerCardsPanel.add(new JLabel(new ImageIcon(card.getImage())));
        }

        revalidate();

        int dealerValue = getValue(dealerCards);

        if (playerValue == 21) {
            restart("won");
        } else if (dealerValue > 21) {
            restart("won");
        } else if (dealerValue > playerValue) {
            restart("lost");
        } else if (playerValue > dealerValue) {
            restart("won");
        } else {
            restart("tied");
        }
    }

    public void check() {
        int value = getValue(playerCards);
        if (value > 21) {
            restart("lost");
        } else if (value == 21) {
            restart("won");
        }
    }
}