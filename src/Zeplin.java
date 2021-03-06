import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

class Zeplin {

    public static String latLong;
    public static String komsuluklar;

    public static List<Sehir> grandPath;
    private static ArrayList<Sehir> sehirler = new ArrayList<>();
    public static int rakimCarpani = 1000; //metre cinsinden (1000 metre = 1 kilometre)
    public static int maxYolcu = 50; //Zeplinin alabilecegi maximum yolcu sayisi
    public static int minYolcu = 5; //Zeplinin alabilecegi minimum yolcu sayisi
    public static double yolcuUcreti = 20.0; //Binis sirasinda alinan ucret
    public static double yolcuAgirlik = 1.0; //Her binen kisi maksimum hareket kabiliyetini kac derece azaltacak
    public static double maxAci = 80.0; //Yolcu binmedigindeki hareket kabiliyeti
    public static double yerdenYukseklik = 50.0; //Kilometre cinsinden
    public static double maliyet = 1000.0 / 100.0; //Fiyat/Kilometre
    public static int baslangic = 1; //Baslangic sehri
    public static int bitis = 1; //Bitis sehri
    public static int yolcu = 5; //Yolcu sayisi

    public static void putGlobals(int rC, int mnY, int mxY, double yU, double yA, double mA, double yY, double m, int y) {
        rakimCarpani = rC;
        maxYolcu = mxY;
        minYolcu = mnY;
        yolcuUcreti = yU;
        yolcuAgirlik = yA;
        maxAci = mA;
        yerdenYukseklik = yY;
        maliyet = m;
        yolcu = y;
    }

    public static Sehir getSehir(int plaka) {
        return sehirler.get(plaka - 1);
    }

    private static String plakadanIsim(String plaka) {
        return ("Adana,Adıyaman,Afyon,Ağrı,Amasya,Ankara," +
                "Antalya,Artvin,Aydın,Balıkesir,Bilecik,Bingöl,Bitlis,Bolu,Burdur," +
                "Bursa,Çanakkale,Çankırı,Çorum,Denizli,Diyarbakır,Edirne,Elazığ," +
                "Erzincan,Erzurum,Eskişehir,Gaziantep,Giresun,Gümüşhane,Hakkari," +
                "Hatay,Isparta,Mersin,İstanbul,İzmir,Kars,Kastamonu,Kayseri,Kırklareli," +
                "Kırşehir,Kocaeli,Konya,Kütahya,Malatya,Manisa,Kahramanmaraş,Mardin," +
                "Muğla,Muş,Nevşehir,Niğde,Ordu,Rize,Sakarya,Samsun,Siirt,Sinop,Sivas," +
                "Tekirdağ,Tokat,Trabzon,Tunceli,Şanlıurfa,Uşak,Van,Yozgat,Zonguldak," +
                "Aksaray,Bayburt,Karaman,Kırıkkale,Batman,Şırnak,Bartın,Ardahan,Iğdır," +
                "Yalova,Karabük,Kilis,Osmaniye,Düzce").split(",")[Integer.parseInt(plaka) - 1];
    }

    private static String plakadanXY(String plaka) {
        return ("512,371;659,335;253,274;906,206;530,149;367,206;225,400;837,106;116,329;" +
                "118,195;235,175;791,252;869,288;326,145;219,351;182,175;62,165;410,143;" +
                "473,156;174,336;774,317;67,88;705,284;698,212;827,185;286,212;628,390;" +
                "673,153;716,169;981,344;558,437;272,325;422,416;186,105;80,287;907,148;" +
                "424,89;526,286;110,62;447,238;233,118;353,319;202,227;653,291;131,261;" +
                "591,330;799,363;134,373;840,259;473,279;469,337;621,138;792,124;261,136;" +
                "533,108;870,327;484,83;611,224;90,108;579,166;737,132;727,245;715,379;" +
                "189,276;953,290;505,218;328,103;426,299;760,170;403,378;421,201;826,321;" +
                "888,355;365,80;893,103;957,181;192,142;369,111;597,406;551,375;297,126")
                .split(";")[Integer.parseInt(plaka) - 1];
    }

    private static ArrayList<String> dosyaOkuma(String dosya) throws Exception {
        //Graf her olusturuldugunda dosyadan okumamasi icin bellege aliyoruz
        if (dosya.equals("latlong.txt") && latLong != null) {
            return new ArrayList<>(Arrays.asList(latLong.split("\n")));
        }
        if (dosya.equals("komsuluklar.txt") && latLong != null) {
            return new ArrayList<>(Arrays.asList(komsuluklar.split("\n")));
        }
        ArrayList<String> satirlar = new ArrayList<>();
        File file = new File(dosya);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        int satirSayisi = 0;
        String satir;
        while ((satir = br.readLine()) != null) {
            satirlar.add(satirSayisi, satir);
            satirSayisi++;
        }
        br.close();
        return satirlar;
    }

    private static double sehirlerArasiUzaklik(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        return (Math.acos(dist) * 1.609344 * 60 * 1.1515 * 180.0 / Math.PI);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static void init() {
        //eskiyi temizle
        sehirler = new ArrayList<>();

        //latlong.txt oku
        try {
            for (String latLong : dosyaOkuma("latlong.txt")) {
                String[] lineToArray = latLong.split(",");
                sehirler.add(new Sehir(lineToArray[2], lineToArray[0], lineToArray[1], lineToArray[3], plakadanIsim(lineToArray[2]), plakadanXY(lineToArray[2])));
            }
        } catch (Exception e) {
            System.out.println("latlong.txt Okunurken Hata!");
            System.exit(41);
        }

        //komsular.txt oku
        try {
            for (String komsuluklar : dosyaOkuma("komsular.txt")) {
                String[] lineToArray = komsuluklar.split(",");
                Sehir anaSehir = getSehir(Integer.parseInt(lineToArray[0]));
                for (int i = 1; i < lineToArray.length; i++) {
                    Sehir komsuSehir = getSehir(Integer.parseInt(lineToArray[i]));
                    double getKM = sehirlerArasiUzaklik(anaSehir.getLat(), anaSehir.getLon(), komsuSehir.getLat(), komsuSehir.getLon());
                    anaSehir.komsuEkle(komsuSehir, getKM);
                }
            }
        } catch (Exception e) {
            System.out.println("komsular.txt Okunurken Hata!");
            System.exit(42);
        }
    }

    private static double ucmaUzakligiBul(Sehir s1, Sehir s2) {
        double ekstra = s1.getPlaka() == baslangic && s2.getPlaka() == bitis ? 0.0 : //Iki sehir komsu ise direk git
                s1.getPlaka() == baslangic ? -yerdenYukseklik : //Sehir 1 baslangic ise
                        s2.getPlaka() == bitis ? yerdenYukseklik : 0.0; //Sehir 2 bitis ise
        double x = s1.uzakliklar.get(s1.komsuluklar.indexOf(s2));
        double y = Math.abs(s1.getRakim() * rakimCarpani * 0.001 - s2.getRakim() * rakimCarpani * 0.001 + ekstra * rakimCarpani * 0.001);
        return Math.sqrt((x * x) + (y * y));
    }

    private static void dijkstra(Sehir baslangic) {
        baslangic.enKisaMesafe = 0.; //Baslangicin en kisa mesafesini 0 atayalim
        PriorityQueue<Sehir> sehirKuyrugu = new PriorityQueue<>(); //Bir sehir oncelik kuyrugu olusturalim ve
        sehirKuyrugu.add(baslangic); //Baslangici ekleyelim
        while (!sehirKuyrugu.isEmpty()) { //Sehir kalmayana kadar dolasip
            Sehir uygunSehir = sehirKuyrugu.poll(); //En dusuk maliyetli (en son gidilen) sehri cekelim
            for (Sehir komsu : uygunSehir.komsuluklar) { //En uygun sehrin komsularina baglantilarini dolasalim
                double uygunSehirUzakligi = uygunSehir.enKisaMesafe + ucmaUzakligiBul(uygunSehir, komsu); //Bu sehre en kisa uzaklik, bu sehre gitmek icin harcanan maliyet + kenar uzakligidir
                if (uygunSehirUzakligi < komsu.enKisaMesafe) { //Eger bu sehre daha onceden daha kisa sekilde gidilememisse
                    sehirKuyrugu.remove(komsu); //Bu sehre en kisa uzakligi bulduk demektir. Simdikini kaldiralim.
                    komsu.enKisaMesafe = uygunSehirUzakligi; //Bunu sehire yazalim
                    komsu.onceki = uygunSehir; //Ve bu sehre bagli sehirlerden en kisasini atayalim
                    sehirKuyrugu.add(komsu);
                }
            }
        }
    }

    private static List<Sehir> enKisaYol(Sehir baslangic) {
        List<Sehir> yol = new ArrayList<>();
        for (Sehir sehir = baslangic; sehir != null; sehir = sehir.onceki)
            yol.add(sehir);

        Collections.reverse(yol);
        return yol;
    }

    private static boolean gidilebilirMi(double x, Sehir s1, Sehir s2, double ekstra, int yolcu) {
        if (s1.getPlaka() == baslangic) {
            ekstra = -ekstra;
        }
        double y = Math.abs(s1.getRakim() * rakimCarpani * 0.001 - s2.getRakim() * rakimCarpani * 0.001 + ekstra * rakimCarpani * 0.001);
        double e = Math.atan2(y, x) * 180 / Math.PI;
        System.out.println("ekstra kalkis    : " + ekstra);
        System.out.println("uzaklik          : " + x);
        System.out.println("yukseklik        : " + y);
        System.out.println("egim             : " + Math.floor(e * 100) / 100 + " (" + s1 + "-" + s2 + ")");
        System.out.println("maximum egim     : " + (maxAci - yolcu * yolcuAgirlik) + "\n");
        return (maxAci - yolcu * yolcuAgirlik) >= e;
    }

    private static Double flyWithCapasity(Sehir baslangic, Sehir bitis, int yolcu) {
        dijkstra(baslangic);
        List<Sehir> path = enKisaYol(bitis);
        if (bitis.enKisaMesafe != Double.POSITIVE_INFINITY) {
            System.out.println("Durum            : Deneniyor...");
            for (int i = 0; i < path.size() - 1; i++) {
                //birinci veya sondan bir onceki ise ekstra +50 rakim ekle (yukselme-alcalma)
                double ekstra = i == 0 || i == path.size() - 2 ? yerdenYukseklik : 0;
                //Eger iki sehir komsu ise kalkis masrafi yok
                if (path.size() == 2) {
                    ekstra = 0;
                }
                Sehir s1 = path.get(i);
                Sehir s2 = path.get(i + 1);
                double uzaklik = s1.uzakliklar.get(s1.komsuluklar.indexOf(s2));
                if (gidilebilirMi(uzaklik, s1, s2, ekstra, yolcu)) {
                    //System.out.println("Gidilebilir: " + s1 + "-" + s2);
                } else {
                    System.out.println("Gidilemeyen Yol  : " + path);
                    System.out.println("Burasi Cok Dik   : " + s1 + "-" + s2 + "\n\n");
                    s1.uzakliklar.set(s1.komsuluklar.indexOf(s2), Double.POSITIVE_INFINITY);
                    s2.uzakliklar.set(s2.komsuluklar.indexOf(s1), Double.POSITIVE_INFINITY);

                    //Yolu tekrar hesaplamak icin tum mesafeleri resetliyoruz
                    for (int j = 0; j < 81; j++) {
                        sehirler.get(j).enKisaMesafe = Double.POSITIVE_INFINITY;
                    }

                    return flyWithCapasity(baslangic, bitis, yolcu);
                }
            }
            System.out.println("Gidilen Yol      : " + path + "\n\n");
            Ekran.logla("Gidilen Yol: " + path);
            grandPath = path;
            return ((Math.round(bitis.enKisaMesafe * 10000) / 10000.0));

        } else {
            System.out.println("Gidilen Yol      : Gidilemedi.\n\n");
            Ekran.logla("Gidilemedi.\n\n");
            return Double.POSITIVE_INFINITY;
        }
    }

    public static String maksimumKarHesapla(int bs, int bt) {
        baslangic = bs;
        bitis = bt;
        grandPath = null;
        double maxKar = Double.NEGATIVE_INFINITY;
        int optimalYolcu = minYolcu;
        for (int i = minYolcu; i <= maxYolcu; i++) {
            init();
            System.out.println("\n\n\n\n" + i + " Kişi için ---------------------------------------");
            Ekran.logla("\n" + i + " Kişi için;");
            double newKM = flyWithCapasity(getSehir(baslangic), getSehir(bitis), i);
            double newKar = (yolcuUcreti * i) - (newKM * maliyet);
            if (maxKar <= newKar) {
                maxKar = newKar;
                optimalYolcu = i;
            }
        }

        init();
        flyWithCapasity(getSehir(baslangic), getSehir(bitis), optimalYolcu);
        if (grandPath != null) { //Yol var ise
            Ekran.drawSmt(grandPath);
            Ekran.logla("\n\n" + optimalYolcu + " yolcu ile gidilen yol: " + grandPath.toString());
            return (optimalYolcu + " yolcu ile yola çıkıldığında firma maksimum kâr'a ulaşır.\n" +
                    "Maksimum kâr: " + maxKar + "TL\n");
        } else {
            Ekran.drawSmt(null);
            return "Buraya hiçbir şekilde gidilemez. \n";
        }
    }

    public static String yuzdeElliKarHesapla(int bs, int bt, int yl) {
        baslangic = bs;
        bitis = bt;
        yolcu = yl;
        if (yolcu >= minYolcu && yolcu <= maxYolcu) {
            init();
            Ekran.logla(yolcu + " yolcu ile;");
            double km = flyWithCapasity(getSehir(bs), getSehir(bt), yolcu);
            if (km != Double.POSITIVE_INFINITY) {
                System.out.println("Toplamda         : " + km + " KM yol gidildi.\n\n");
                Ekran.logla("Toplamda " + km + " KM yol gidildi.");
                double seferMaliyeti = maliyet * km;
                double seferBasiYolcuUcreti = seferMaliyeti / yolcu;
                Ekran.drawSmt(grandPath);
                return "Kişi başı " + seferBasiYolcuUcreti * 2 + "TL alındığında firma %50 kâr'a ulaşır."; //Yuzde elli kar
            } else {
                return "";
            }

        } else {
            return "Yolcu sayisi verilen aralikta degil. (Girilen:" + yolcu + ", Aralik:[" + minYolcu + "," + maxYolcu + "])";
        }
    }
}

class Sehir implements Comparable<Sehir> {
    private final String plaka;
    private final String isim;
    private final double lat;
    private final double lon;
    private final double rakim;
    private final int x;
    private final int y;
    public ArrayList<Sehir> komsuluklar = new ArrayList<>();
    public ArrayList<Double> uzakliklar = new ArrayList<>();
    public double enKisaMesafe = Double.POSITIVE_INFINITY;
    public Sehir onceki;

    public void komsuEkle(Sehir komsu, Double uzaklik) {
        komsuluklar.add(komsu);
        uzakliklar.add(uzaklik);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getRakim() {
        return rakim;
    }

    public int getPlaka() {
        return Integer.parseInt(plaka);
    }

    public Sehir(String plaka, String lat, String lon, String rakim, String isim, String xy) {
        this.plaka = plaka;
        this.isim = isim;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
        this.rakim = Double.parseDouble(rakim);
        this.x = Integer.parseInt(xy.split(",")[0]);
        this.y = Integer.parseInt(xy.split(",")[1]);
    }

    public String toString() {
        return isim;
    }

    public int compareTo(Sehir diger) {
        return Double.compare(enKisaMesafe, diger.enKisaMesafe);
    }
}

class Ekran extends JFrame {

    public static int yazilacakSehir = 1; //Tek sayilar birinci, Cift sayilar ikinci sehri aktive eder
    public static JPanel anaPanel = new JPanel();
    public static cizilebilirHarita harita = new cizilebilirHarita();
    public static JTextArea log = new JTextArea();


    //Renkler
    //Kaynak: https://coolors.co/f45b69-f6e8ea-22181c-5a0001-f13030
    public static Color Red = new Color(0xF13030);
    public static Color ActiveRed = new Color(0xF24242);
    public static Color FocusRed = new Color(0xF45B69);
    public static Color White = new Color(0xffffff);
    public static Color NotSoWhite = new Color(0xcccccc);
    public static Color Background = new Color(0x22181C);
    public static Color DarkBackground = new Color(0x191215);


    //Kaynak: https://stackoverflow.com/questions/27706197/how-can-i-center-graphics-drawstring-in-java
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }


    public static class stilliButon extends JButton {
        public static Color COLOR = Red;
        public String text = "";
        public boolean active = false;

        @Override
        public String toString() {
            return this.text;
        }

        public stilliButon(String text) {
            this.text = text;
            setBorderPainted(false);
            setOpaque(false);
            setBackground(Red);
            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    setBackground(ActiveRed);
                }

                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    setBackground(ActiveRed);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    setBackground(Red);
                }

                public void mousePressed(java.awt.event.MouseEvent evt) {
                    setBackground(Red);
                }
            });
        }

        @Override
        public void setBackground(Color c) {
            if (!this.active) {
                COLOR = c;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            RenderingHints qualityHints =
                    new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHints(qualityHints);
            g2.setPaint(COLOR);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.setPaint(White);
            drawCenteredString(g2, this.text, new Rectangle(getWidth(), getHeight()), getMontserrat("SemiBold"));
            g2.dispose();
        }

    }

    public static class altiCizgiliInput extends JTextField {
        public altiCizgiliInput(String ad, Dimension dim, int x, int y, String initialValue) {
            setCaretColor(White);
            setForeground(White);
            setOpaque(false);
            setFont(getMontserrat("Regular"));
            setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
            JLabel label = new JLabel(ad);
            setSize(dim);
            setLocation(x, y);
            anaPanel.add(this);
            label.setForeground(NotSoWhite);
            label.setFont(getMontserrat("Regular"));
            label.setSize(dim);
            label.setLocation(x, y - 20);
            setText(initialValue);
            anaPanel.add(label);
        }

        public altiCizgiliInput() {
            setCaretColor(White);
            setForeground(White);
            setOpaque(false);
            setFont(getMontserrat("Regular"));
            setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
        }
    }

    //Kaynak: https://docs.oracle.com/javase/7/docs/api/javax/swing/plaf/basic/BasicScrollBarUI.html
    public static class sbUI extends BasicScrollBarUI {
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton jbutton = new JButton();
            jbutton.setPreferredSize(new Dimension(0, 0));
            jbutton.setMinimumSize(new Dimension(0, 0));
            jbutton.setMaximumSize(new Dimension(0, 0));
            return jbutton;
        }

        @Override
        protected void configureScrollBarColors() {
            scrollBarWidth = 5;
            this.thumbHighlightColor = Red;
            this.thumbLightShadowColor = Red;
            this.thumbDarkShadowColor = Red;
            this.thumbColor = Red;
            this.trackColor = DarkBackground;
            this.trackHighlightColor = Red;
        }
    }

    public static class cizilebilirHarita extends JPanel {

        private BufferedImage image;

        public cizilebilirHarita() {
            try {
                image = ImageIO.read(new File(".\\tr.png"));
                // TODO Haritayı Yükleyin
            } catch (IOException e) {
                e.printStackTrace();
            }
            image = process(image);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
        }


        private BufferedImage process(BufferedImage old) {
            int w = old.getWidth();
            int h = old.getHeight();
            BufferedImage img = new BufferedImage(
                    w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.drawImage(old, 0, 0, w, h, this);

            g2d.dispose();
            return img;
        }

        public void drawNew() {
            try {
                image = ImageIO.read(new File(".\\tr.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void drawSmt(int x1, int y1, int x2, int y2) {
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setPaint(Red);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(new Line2D.Float(x1, y1, x2, y2));
            this.image = this.process(image);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }
    }


    //Kaynak: https://docs.oracle.com/javase/tutorial/2d/text/fonts.html
    public static Font getMontserrat(String boldness) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(".\\Montserrat-" + boldness + ".ttf"));
            return font.deriveFont(Font.PLAIN, 13);
        } catch (Exception e) {
            System.out.println("Font yuklenirken hata!");
            return new Font("Arial", Font.PLAIN, 13);
        }
    }

    public static void rotaEkle(Sehir sehir, altiCizgiliInput s1, altiCizgiliInput s2) {
        if (yazilacakSehir % 2 == 0) {
            s1.setText(sehir.getPlaka() + "");
            s1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
            s2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, FocusRed));
        } else {
            s2.setText(sehir.getPlaka() + "");
            s1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, FocusRed));
            s2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
        }
    }

    public static void drawSmt(List<Sehir> grandPath) {
        harita.drawNew();
        if (grandPath != null) {
            for (int i = 0; i < grandPath.size() - 1; i++) {
                harita.drawSmt(
                        grandPath.get(i).getX(),
                        grandPath.get(i).getY(),
                        grandPath.get(i + 1).getX(),
                        grandPath.get(i + 1).getY()
                );
            }
        }
        anaPanel.repaint();
    }

    public static void logla(String x) {
        log.setText(log.getText() + x + "\n");
    }

    public static void logla(String x, String y) {
        log.setText(log.getText() + x + y);
    }

    public static void logClr() {
        log.setText("");
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("Gezgin Zeplin");
        frame.setSize(1024, 760);

        //https://stackoverflow.com/questions/2442599/how-to-set-jframe-to-appear-centered-regardless-of-monitor-resolution
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(screenSize.width / 2 - frame.getSize().width / 2, screenSize.height / 2 - frame.getSize().height / 2);
        //---------

        anaPanel.setLayout(null);

        harita.setSize(1024, 500);
        harita.setLocation(0, 0);
        harita.setBackground(Background);

        altiCizgiliInput sehir1 = new altiCizgiliInput("Bşlm", new Dimension(40, 30), 50, 650, "");
        altiCizgiliInput sehir2 = new altiCizgiliInput("Bitş", new Dimension(40, 30), 110, 650, "");
        sehir1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, FocusRed));
        altiCizgiliInput yolcuSayisi = new altiCizgiliInput("Yolcu Sayısı", new Dimension(100, 30), 50, 500, "5");
        altiCizgiliInput rakimCarpani = new altiCizgiliInput("Rakım Çarpanı", new Dimension(100, 30), 50, 550, "1000");
        altiCizgiliInput yerdenYukseklik = new altiCizgiliInput("Yükseklik", new Dimension(100, 30), 50, 600, "50");
        altiCizgiliInput maxYolcu = new altiCizgiliInput("Max Yolcu", new Dimension(100, 30), 170, 500, "50");
        altiCizgiliInput minYolcu = new altiCizgiliInput("Min Yolcu", new Dimension(100, 30), 170, 550, "5");
        altiCizgiliInput yolcuUcreti = new altiCizgiliInput("Yolcu Ücreti", new Dimension(100, 30), 170, 600, "20");
        altiCizgiliInput yolcuAgirlik = new altiCizgiliInput("Yolcu Ağırlık", new Dimension(100, 30), 290, 500, "1");
        altiCizgiliInput maxAci = new altiCizgiliInput("Max Açı", new Dimension(100, 30), 290, 550, "80");
        altiCizgiliInput maliyet = new altiCizgiliInput("Maliyet", new Dimension(100, 30), 290, 600, "10");

        //Log kısmı
        JScrollPane sp = new JScrollPane(log);
        DefaultCaret caret = (DefaultCaret) log.getCaret();
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(Background);
        log.setForeground(White);
        log.setFont(getMontserrat("Regular"));
        log.setCaretColor(White);
        sp.getVerticalScrollBar().setUI(new sbUI());
        sp.getVerticalScrollBar().setBorder(BorderFactory.createEmptyBorder());
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setLocation(420, 505);
        sp.setSize(550, 175);
        sp.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JLabel label = new JLabel();
        label.setText("Log");
        label.setForeground(NotSoWhite);
        label.setFont(getMontserrat("Regular"));
        label.setSize(550, 30);
        label.setLocation(420, 480);
        anaPanel.add(label);
        anaPanel.add(sp);


        stilliButon hesapla = new stilliButon("Max. Hesapla");
        hesapla.setSize(100, 30);
        hesapla.setLocation(170, 650);

        stilliButon hesapla2 = new stilliButon("%50 Hesapla");
        hesapla2.setSize(100, 30);
        hesapla2.setLocation(290, 650);

        //Sehir butonlarını koy
        Zeplin.init();
        for (int k = 0; k < 1; k++) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    stilliButon sehirButton = new stilliButon(((j * 9) + i + 1) + "");
                    sehirButton.setSize(20, 20);
                    int x = Zeplin.getSehir((j * 9) + i + 1).getX();
                    int y = Zeplin.getSehir((j * 9) + i + 1).getY();
                    sehirButton.setLocation(x - 10, y - 10); //-20(boyut)/2 (ortalamak icin)
                    sehirButton.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mousePressed(java.awt.event.MouseEvent evt) {
                            yazilacakSehir++;
                            rotaEkle(Zeplin.getSehir(Integer.parseInt(evt.getComponent().toString())), sehir1, sehir2);
                        }
                    });
                    anaPanel.add(sehirButton);
                }
            }
        }


        sehir1.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (yazilacakSehir % 2 == 0) {
                    yazilacakSehir++;
                    sehir2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
                    sehir1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, FocusRed));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        sehir2.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (yazilacakSehir % 2 == 1) {
                    yazilacakSehir++;
                    sehir1.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Red));
                    sehir2.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, FocusRed));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

        hesapla.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!(rakimCarpani.getText().equals("") ||
                        minYolcu.getText().equals("") ||
                        maxYolcu.getText().equals("") ||
                        yolcuUcreti.getText().equals("") ||
                        yolcuAgirlik.getText().equals("") ||
                        maxAci.getText().equals("") ||
                        yerdenYukseklik.getText().equals("") ||
                        sehir1.getText().equals("") ||
                        sehir2.getText().equals("") ||
                        maliyet.getText().equals("") ||
                        yolcuSayisi.getText().equals(""))) {

                    Zeplin.putGlobals(
                            Integer.parseInt(rakimCarpani.getText()),
                            Integer.parseInt(minYolcu.getText()),
                            Integer.parseInt(maxYolcu.getText()),
                            Double.parseDouble(yolcuUcreti.getText()),
                            Double.parseDouble(yolcuAgirlik.getText()),
                            Double.parseDouble(maxAci.getText()),
                            Double.parseDouble(yerdenYukseklik.getText()),
                            Double.parseDouble(maliyet.getText()),
                            Integer.parseInt(yolcuSayisi.getText())
                    );
                    logClr();
                    logla(Zeplin.maksimumKarHesapla(Integer.parseInt(sehir1.getText()), Integer.parseInt(sehir2.getText())));
                } else {
                    logla("\nZorunlu alanlardan en az biri boş gibi görünüyor.");
                }
            }
        });

        hesapla2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (!(rakimCarpani.getText().equals("") ||
                        minYolcu.getText().equals("") ||
                        maxYolcu.getText().equals("") ||
                        yolcuUcreti.getText().equals("") ||
                        yolcuAgirlik.getText().equals("") ||
                        maxAci.getText().equals("") ||
                        yerdenYukseklik.getText().equals("") ||
                        sehir1.getText().equals("") ||
                        sehir2.getText().equals("") ||
                        maliyet.getText().equals("") ||
                        yolcuSayisi.getText().equals(""))) {

                    Zeplin.putGlobals(
                            Integer.parseInt(rakimCarpani.getText()),
                            Integer.parseInt(minYolcu.getText()),
                            Integer.parseInt(maxYolcu.getText()),
                            Double.parseDouble(yolcuUcreti.getText()),
                            Double.parseDouble(yolcuAgirlik.getText()),
                            Double.parseDouble(maxAci.getText()),
                            Double.parseDouble(yerdenYukseklik.getText()),
                            Double.parseDouble(maliyet.getText()),
                            Integer.parseInt(yolcuSayisi.getText())
                    );
                    logClr();
                    logla(Zeplin.yuzdeElliKarHesapla(Integer.parseInt(sehir1.getText()), Integer.parseInt(sehir2.getText()), Integer.parseInt(yolcuSayisi.getText())), "");
                } else {
                    logla("\nZorunlu alanlardan en az biri boş gibi görünüyor.");
                }
            }
        });


        anaPanel.add(harita);
        anaPanel.add(hesapla);
        anaPanel.add(hesapla2);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        anaPanel.setBackground(Background);
        frame.setResizable(false);
        frame.setContentPane(anaPanel);
        frame.setVisible(true);


    }
}
