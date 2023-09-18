import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Resimlerin alındığı kısım
            BufferedImage referansResim = ImageIO.read(new File("dnyan.jpg")); // Referans resim
            File klasor = new File("resimler"); // Karşılaştırılacak resimlerin bulunduğu klasör
            FilenameFilter dosyaFiltresi = new FilenameFilter() { // ".jpg" uzantılı dosyaları seçmek için dosya filtresi oluştur
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".jpg");
                }
            };
            // Klasördeki ".jpg" dosyalarını listeyen kısım
            File[] resimDosyalari = klasor.listFiles(dosyaFiltresi);

            if (resimDosyalari != null) {
                List<Result> results = new ArrayList<>();

                for (File resimDosyasi : resimDosyalari) {
                    BufferedImage resim2 = ImageIO.read(resimDosyasi); // Karşılaştırılacak resmi yükle

                    // resim boyutlarını eşitle
                    int maxWidth = Math.max(referansResim.getWidth(), resim2.getWidth());
                    int maxHeight = Math.max(referansResim.getHeight(), resim2.getHeight());
                    referansResim = resizeImage(referansResim, maxWidth, maxHeight);

                    double maxBenzerlikOrani = 0;
                    double simetriBenzerlikOrani = 0;
                    double dikeyBenzerlikOrani = 0;

                    // Resmi 45 derece aralıklarla döndürerek benzerlik kontrolü yap istersek açıyı 1 derece yapabiliriz
                    for (int i = 0; i < 360; i += 45) {
                        BufferedImage dondurulmusResim = rotateImage(resim2, i); // Resmi döndür
                        dondurulmusResim = resizeImage(dondurulmusResim, maxWidth, maxHeight); // Boyutları eşitle

                        int farkliPixeller = 0;

                        // Farklı pikselleri sayarak benzerlik oranını hesaplar
                        for (int y = 0; y < maxHeight; y++) {
                            for (int x = 0; x < maxWidth; x++) {
                                int piksel1 = referansResim.getRGB(x, y);
                                int piksel2 = dondurulmusResim.getRGB(x, y);

                                if (piksel1 != piksel2) {
                                    farkliPixeller++;
                                }
                            }
                        }

                        double benzerlikOrani = ((double) (maxWidth * maxHeight - farkliPixeller) / (maxWidth * maxHeight)) * 100;

                        if (benzerlikOrani > maxBenzerlikOrani) {
                            maxBenzerlikOrani = benzerlikOrani;
                        }
                    }

                    // Resim2'nin simetriğini al
                    BufferedImage simetrikResim2 = flipImage(resim2);
                    simetrikResim2 = resizeImage(simetrikResim2, maxWidth, maxHeight); // Boyutları eşitle

                    int farkliPikseller = 0;

                    // Farklı pikselleri sayarak simetrik benzerlik oranını hesaplar
                    for (int y = 0; y < maxHeight; y++) {
                        for (int x = 0; x < maxWidth; x++) {
                            int piksel1 = referansResim.getRGB(x, y);
                            int piksel2 = simetrikResim2.getRGB(x, y);

                            if (piksel1 != piksel2) {
                                farkliPikseller++;
                            }
                        }
                    }

                    simetriBenzerlikOrani = ((double) (maxWidth * maxHeight - farkliPikseller) / (maxWidth * maxHeight)) * 100;

                    // Dikey simetriğini al
                    BufferedImage dikeySimetrikResim2 = flipImageVertically(resim2);
                    dikeySimetrikResim2 = resizeImage(dikeySimetrikResim2, maxWidth, maxHeight); // Boyutları eşitle

                    int farkliPiksellerDikey = 0;

                    // Farklı pikselleri sayarak dikey simetri benzerlik oranını hesaplar
                    for (int y = 0; y < maxHeight; y++) {
                        for (int x = 0; x < maxWidth; x++) {
                            int piksel1 = referansResim.getRGB(x, y);
                            int piksel2 = dikeySimetrikResim2.getRGB(x, y);

                            if (piksel1 != piksel2) {
                                farkliPiksellerDikey++;
                            }
                        }
                    }

                    dikeyBenzerlikOrani = ((double) (maxWidth * maxHeight - farkliPiksellerDikey) / (maxWidth * maxHeight)) * 100;

                    Result result = new Result(resimDosyasi.getName(), maxBenzerlikOrani, simetriBenzerlikOrani, dikeyBenzerlikOrani);
                    results.add(result);
                }

                // Sonuçları Swing ile gösterir
                showResults(results);
            } else {
                System.out.println("Klasörde resim dosyası bulunamadı.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Resmi yeniden boyutlandırmak için kullanılan metot
    public static BufferedImage resizeImage(BufferedImage orijinalResim, int genislik, int yukseklik) {
        BufferedImage yenidenBoyutlandirilmisResim = new BufferedImage(genislik, yukseklik, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = yenidenBoyutlandirilmisResim.createGraphics();
        g2d.drawImage(orijinalResim, 0, 0, genislik, yukseklik, null);
        g2d.dispose();
        return yenidenBoyutlandirilmisResim;
    }

    // Resmi belirtilen dereceye göre çeviren metot
    public static BufferedImage rotateImage(BufferedImage orijinalResim, double derece) {
        double radyanlar = Math.toRadians(derece);
        double sin = Math.abs(Math.sin(radyanlar));
        double cos = Math.abs(Math.cos(radyanlar));
        int yeniGenislik = (int) Math.round(orijinalResim.getWidth() * cos + orijinalResim.getHeight() * sin);
        int yeniYukseklik = (int) Math.round(orijinalResim.getWidth() * sin + orijinalResim.getHeight() * cos);

        BufferedImage dondurulmusResim = new BufferedImage(yeniGenislik, yeniYukseklik, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dondurulmusResim.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate((yeniGenislik - orijinalResim.getWidth()) / 2, (yeniYukseklik - orijinalResim.getHeight()) / 2);
        g2d.rotate(radyanlar, orijinalResim.getWidth() / 2, orijinalResim.getHeight() / 2);
        g2d.drawRenderedImage(orijinalResim, null);
        g2d.dispose();
        return dondurulmusResim;
    }

    // Resmi yatay olarak simetrik hale getiren metot
    public static BufferedImage flipImage(BufferedImage orijinalResim) {
        int genislik = orijinalResim.getWidth();
        int yukseklik = orijinalResim.getHeight();
        BufferedImage simetrikResim = new BufferedImage(genislik, yukseklik, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = simetrikResim.createGraphics();
        g2d.drawImage(orijinalResim, 0, 0, genislik, yukseklik, genislik, 0, 0, yukseklik, null);
        g2d.dispose();
        return simetrikResim;
    }

    // Resmi dikey olarak simetrik hale getiren metot
    public static BufferedImage flipImageVertically(BufferedImage orijinalResim) {
        int genislik = orijinalResim.getWidth();
        int yukseklik = orijinalResim.getHeight();
        BufferedImage dikeySimetrikResim = new BufferedImage(genislik, yukseklik, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dikeySimetrikResim.createGraphics();
        g2d.drawImage(orijinalResim, 0, 0, genislik, yukseklik, 0, yukseklik, genislik, 0, null);
        g2d.dispose();
        return dikeySimetrikResim;
    }

    // Sonuçları Swing ile gösteren metot
    public static void showResults(List<Result> results) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Benzerlik Sonuçları");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel(new GridLayout(results.size(), 1));

            for (Result result : results) {
                JLabel label = new JLabel(result.getName() + " - Benzerlik Oranı: %" + result.getMaxSimilarity()
                        + " - Simetri Benzerlik Oranı: %" + result.getSymmetricSimilarity()
                        + " - Dikey Simetri Benzerlik Oranı: %" + result.getVerticalSymmetricSimilarity());
                panel.add(label);
            }

            JScrollPane scrollPane = new JScrollPane(panel);
            frame.add(scrollPane, BorderLayout.CENTER);

            frame.setSize(400, 400);
            frame.setVisible(true);
        });
    }
}

class Result {
    private String name;
    private double maxSimilarity;
    private double symmetricSimilarity;
    private double verticalSymmetricSimilarity; // Dikey simetri benzerlik oranı

    public Result(String name, double maxSimilarity, double symmetricSimilarity, double verticalSymmetricSimilarity) {
        this.name = name;
        this.maxSimilarity = maxSimilarity;
        this.symmetricSimilarity = symmetricSimilarity;
        this.verticalSymmetricSimilarity = verticalSymmetricSimilarity;
    }

    public String getName() {
        return name;
    }

    public double getMaxSimilarity() {
        return maxSimilarity;
    }

    public double getSymmetricSimilarity() {
        return symmetricSimilarity;
    }

    public double getVerticalSymmetricSimilarity() {
        return verticalSymmetricSimilarity;
    }
}