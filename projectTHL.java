//Hicham SAIDI  ----- (Groupe 3)
//Cheikh Anis   ----- (Groupe 4)
import javax.swing.JFrame;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.List;

class projectTHL {
    static String[] T = {"a", "b", "c"};
    String[] N = {"S", "A"};

    //Pour assurer l'apssance des caractere qui n'apartain pas a l'ensemble des termineaux T
    //Car l'automate a pile ne test pas si les caractere n'exist pas dans les termineaux
    public static boolean inLangage (String mot) {
        int  fin = mot.length() - 1;
        for (int i = 0; i <= fin; i++) if (mot.charAt(i) != 'a' && mot.charAt(i) != 'b' && mot.charAt(i) != 'c') return false;
        return true;
    }
    
    // PARTIE 0: FONCTION PREDEFINIE SUR LES MOTS ET LES LANGAGES -----------------------------------------------------------------------------------
    //methode Miroir donne le miroir d'un mot
    public static String Mir (String mot) {
        //dans cette fonction on vas tester d'abord si le mot longeur ne sont pas null car la fonction est recurcive
        //on vas lire le dèrnier caractaire du mot et on le concatè avec le miroir du se mot sans son dèrnier caractère
        //ensuite on enleve le dèrnier caractaire du mot original et refait la fonction Mir on utilisons le nouveau mot comme paramètre
        int longeur = mot.length();
        if (mot != null && longeur != 0) {
            return mot.charAt(longeur - 1) + Mir(mot.substring(0, longeur - 1));
        }
        return mot;
    }

    //Fonction qui fait la concatenation entre deux langage donner L1 et L2
    public static List<String> concatLangage (List<String> L1, List<String> L2, int startConcatIndex) {
        //startConcatIndex = 1 si le langage contien ε
        //startConcatIndex = 0 si non
        //Pour éviter la redondonce dans le langage générer
        List<String> concat = new ArrayList<>();
        if (startConcatIndex == 1) concat.add(L1.get(0));
        for (int i = startConcatIndex; i < L1.size(); i++) {
            for (int j = 0; j < L2.size(); j++) {
                concat.add(L1.get(i).concat(L2.get(j)));
            }
        }
        return concat;
    }

    //methode de génération du langage
    public static void A(List<String> langage, String mot) {
        langage.add("a" + mot + "aa");
        langage.add("bb" + mot + "b");
    }

    //PARTIE 1: LANGAGE ET GRAMMAIRES --------------------------------------------------------------------------------------------------------------
    //1)
    public static List<String> GeneratorK(int k) {
        //dans le cas ou k <= 0 and ne peut pas générer le langage L(G).
        //Car il n'exist pas des mot avec une longeur moin que 0!
        if (k < 0) {
            System.out.println("\u001B[31mla langueur d'un mot n'est jamais moin que 0!\u001B[0m");
            return null;
        }
        //Declaration des variables
        List<String> langage= new ArrayList<>();
        int start = 1;

        langage.add("");//"\u03B5" -> epsilon 
        if (k >= 1) langage.add("c");
        //Donner au k une valeur exact c'est a dire une valeur qu'un mot peut avoir dans le langage L(G)
        k -= (k%3 == 0) ? 2 : (k%3 == 2) ? 1 : 0; 
        //génération de tous les mots du langage L(G) pour k>=0 et |w|<=k
        while (langage.get(langage.size() - 1).length() < k) {
            int fin = langage.size();
            for (int i = start; i < fin; i++) {
                A(langage, langage.get(i));
            }
            start = fin;
        }
        return langage;
    }
    //2)
    public static List<String> GeneratorR(int k) {
        if (k < 0) {
            System.out.println("\u001B[31mla langueur d'un mot n'est jamais moin que 0!\u001B[0m");
            return null;
        }
        List<String> langageMir = new ArrayList<>();
        List<String> langage = new ArrayList<>();
        
        langage = GeneratorK(k);
        int fin = langage.size();
        for (int i = 0; i < fin; i++)   langageMir.add(Mir(langage.get(i)));

        return langageMir;
    }
    //3)
    public static List<String> GeneratorKn(int k, int n) {
        //Si k ou n est moin que 0 alors on ne peut pas générer le langage 
        if (n < 0 || k < 0) return null;
        //Si n == 0 la fonction vas retourner un langage avec un seul élément "le mot vide"
        else if (n == 0) return GeneratorK(0);
        //Si n == 1 la fonction vas retourner Lk(G) pour arreter la recurciviter
        else if (n == 1) return GeneratorK(k);
        //else (n > 1) on va concatane le langage Lk(G) avec (Lk(G)) puissance n-1
        return concatLangage(GeneratorK(k), GeneratorKn(k , n-1), 1);
                                                                //startConcatIndex = 1 pour ne pas avoir le problem de redandance dans le langage générer
    }

    //PARTIE 2: ANALYSEUR SYNTAXIQUE --------------------------------------------------------------------------------------------------------------    
    //Methode 1 (complexe a expliquer mais rapide dans l'execution) : Utilisons un Automate a pile
    /*
     * c'est un automate a pile:
     * A = <T, P, Q, M, S>
     * T = {ε, a, b, c}
     * P = {⊥, a, b, aa, bb}
     * Q = {S, A}
     * S
     * M =
     * (S, a, ⊥) -> (S, empiler(aa))
     * (S, b, ⊥) -> (S, empiler(b))
     * (S, a, b) -> (S, empiler(aa))
     * (S, b, a) -> (S, empiler(b))
     * (S, a, a) -> (S, empiler(aa))
     * (S, b, bb)  -> (S, empiler(b))
     * (S, b, b)   -> (S, (dépiler(b), empiler(bb)))
     * 
     * (S, c, ⊥) -> (A, )
     * (S, c, aa) -> (A, )
     * (S, c, b) -> (A, )
     * (S, c, bb) -> (A, )
     * 
     * (A, a, aa) -> (A, (dépiler(aa), empiler(a)))
     * (A, a, a)  -> (A, dépiler(a))
     * (A, b, b) -> (A, dépiler(b))
     */
    public static String setWrod(String mot) {
        if (mot.length() > 1)       mot = mot.substring(1);
        else if (mot.length() == 1) mot = "";
        return mot;
    }

    public static boolean analiseurSyntaxiqueAutomatePile(String mot) {
        if (!inLangage(mot)) return false;
        Stack<String> pile = new Stack<>();
        char etat = 'S';
        boolean b = true;
        while (b == true && !mot.isEmpty()) {
            //(S, a, ⊥)   -> (S, empiler(aa))
            if      (etat == 'S' && mot.charAt(0) == 'a' && pile.empty()) {  
                mot = setWrod(mot);  
                pile.push("aa");
            }
            //(S, b, ⊥)   -> (S, empiler(bb)) 
            else if (etat == 'S' && mot.charAt(0) == 'b' && pile.empty()) {
                mot = setWrod(mot);
                pile.push("bb");
            }
            //(S, c, ⊥)   -> (A, )
            else if      (etat == 'S' && mot.charAt(0) == 'c' && pile.empty())   {
                mot = setWrod(mot);
                etat = 'A';
            }
            else if (!pile.empty()) {
                //(S, a, b)   -> (S, empiler(aa))
                if      (etat == 'S' && mot.charAt(0) == 'a' && pile.peek().equals("b")) {
                    mot = setWrod(mot);
                    pile.push("aa");
                }
                //(S, b, a)   -> (S, empiler(bb))
                else if (etat == 'S' && mot.charAt(0) == 'b' && pile.peek().equals("aa")) {
                    mot = setWrod(mot);
                    pile.push("bb");
                }
                //(S, a, aa)   -> (S, empiler(aa))
                else if (etat == 'S' && mot.charAt(0) == 'a' && pile.peek().equals("aa")) {
                    mot = setWrod(mot);
                    pile.push("aa");
                }
                //(S, b, bb)   -> (S, (dépiler(bb), empiler(b)))
                else if (etat == 'S' && mot.charAt(0) == 'b' && pile.peek().equals("bb")) {
                    mot = setWrod(mot);
                    pile.pop();
                    pile.push("b");
                }
                //(S, b, b)  -> (S, empiler(bb))
                else if (etat == 'S' && mot.charAt(0) == 'b' && pile.peek().equals("b")) {
                    mot = setWrod(mot);
                    pile.push("bb");
                }
                //(S, c, aa)  -> (A, )
                else if      (etat == 'S' && mot.charAt(0) == 'c' && pile.peek().equals("aa")) {
                    mot = setWrod(mot);
                    etat = 'A';
                }
                //(S, c, b)   -> (A, )
                else if (etat == 'S' && mot.charAt(0) == 'c' && pile.peek().equals("b"))  {
                    mot = setWrod(mot);
                    etat = 'A';
                }      
                //(S, c, bb) -> (A, dépiler(b))           
                else if (etat == 'S' &&  mot.charAt(0) == 'c' &&pile.peek().equals("bb")) {
                    mot = setWrod(mot);
                    etat = 'A';
                    return false;
                }
                //(A, a, aa)   -> (A, (dépiler(aa), empiler(a)))
                else if      (etat == 'A' && mot.charAt(0) == 'a' && pile.peek().equals("aa")) {
                    mot = setWrod(mot);
                    pile.pop();
                    pile.push("a");
                }
                //(A, a, a)   -> (A, dépiler(a))
                else if (etat == 'A' && mot.charAt(0) == 'a' && pile.peek().equals("a")) {
                    mot = setWrod(mot);                    
                    pile.pop();
                }
                //(A, b, b)   -> (A, dépiler(b))
                else if (etat == 'A' && mot.charAt(0) == 'b' && pile.peek().equals("b")) {
                    mot = setWrod(mot);
                    pile.pop();
                }
            }      
            if ((mot.isEmpty() || etat == 'A') && pile.empty()) b = false;
        }
        if (mot.isEmpty() && pile.empty()) return true;
        return false;
    }
    //Methode 2 (simple a expliquer mais lente dans l'execution) : Utilisons les methodes de la question 1
    /*
        On génére un langage L(k) avec k = mot.length
        En suit en test si le mot exite dans le langage 
     */
    public static boolean analiseurSyntaxiqueSimple (String mot) {
        List<String> L = new ArrayList<>();
        L = GeneratorK(mot.length());
        return L.contains(mot);
    }

    //TEST DES FONCTION ------------------------------------------------------------------------------------------------------------------------------------
    public static void main (String[] args) throws IOException {
        String index = "\u03B5";
        //changer speed a 0 pour avoir une vittece d'écriture normale 
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create a new JFrame (window)
                JFrame frame = new JFrame("Project THL (Hicham_SAIDI_G3 / Anis_CHEIKH_G4)");
                /*
                // Create a JTextField (input field) to read user input
                JTextField textField = new JTextField(20);                

                // Create a JButton (button) to submit the input
                JButton button = new JButton("Submit");

                // Create a JLabel (label) to display the result
                JLabel[] noneText = {new JLabel("  "), new JLabel("  ")};
                */
                // Create a JPanel (panel) to hold the components
                JPanel panel = new JPanel();

                JPanel Ypanel = new JPanel();
                Ypanel.setLayout(new BoxLayout(Ypanel, BoxLayout.Y_AXIS));

                JPanel Ypanel1 = new JPanel();
                Ypanel1.setLayout(new BoxLayout(Ypanel1, BoxLayout.Y_AXIS));

                JPanel Xpanel1 = new JPanel();
                Xpanel1.setLayout(new BoxLayout(Xpanel1, BoxLayout.X_AXIS));

                JPanel leftPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel pdf1 = new JLabel("Soit ℒ(𝐺) le langage généré par la grammaire 𝐺 = <𝑇, 𝑁, 𝑆, 𝑃> tel que :");
                JLabel pdf2 = new JLabel("𝑇 = {𝑎, 𝑏, 𝑐} 𝑁={𝑆,𝐴}");
                JLabel pdf3 = new JLabel("P : 𝑆 → 𝐴 / 𝜀");
                JLabel pdf4 = new JLabel("      𝐴 → 𝑎𝐴𝑎𝑎 / 𝑏𝑏𝐴𝑏 / 𝑐");
                Ypanel.add(pdf1);
                Ypanel.add(pdf2);
                Ypanel.add(pdf3);
                Ypanel.add(pdf4);
                JLabel partie1 = new JLabel("\nPARTIE 1: LANGAGE ET GRAMMAIRES:");
                partie1.setForeground(new Color(10, 10, 170));
                JLabel exo1 = new JLabel("1- Générer tous les mots du langage Lk = {w dans L(G) / |w| ≤ k} pour k ≥ 0:");
                exo1.setForeground(new Color(0,100,0));
                JLabel exo1msg = new JLabel("Entrer la valeur du k = ");
                JTextField exo1k = new JTextField(20);
                exo1k.setPreferredSize(new Dimension(20, 20));
                JButton exe1Button = new JButton("EXE");

                JTextArea exe1Print = new JTextArea();
                exe1Print.setLineWrap(true); // Enable line wrapping
                exe1Print.setWrapStyleWord(true);
                
                exe1Button.addActionListener(e -> {
                    String textInt = exo1k.getText();
                    try {
                        int k = Integer.parseInt(textInt); // Parse the string to an integer
                        if (k < 0) {
                            exe1Print.setText("la langueur d'un mot n'est jamais moin que 0!");
                            exe1Print.setForeground(Color.red);
                        }
                        else {
                             List<String> L1 = GeneratorK(k);
                            L1.set(0, index);
                            exe1Print.setText(L1.toString());
                        }                        
                    } catch (NumberFormatException ex) {
                        exe1Print.setText("Invalid input"); // Display an error message for invalid input
                    }                    
                });
                
                JScrollPane scrollPane1 = new JScrollPane(exe1Print);
                scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane1.setPreferredSize(new Dimension(100, 100)); // Set preferred size for scroll pane


                Ypanel1.add(partie1);
                Ypanel1.add(Box.createVerticalStrut(10));
                Ypanel1.add(exo1);
                
                leftPanel1.add(exo1msg);
                leftPanel1.add(exo1k);
                leftPanel1.add(exe1Button);

                Ypanel1.add(leftPanel1);
                Ypanel1.add(scrollPane1);
                Ypanel.add(Ypanel1);
                

                JPanel Ypanel2 = new JPanel();
                Ypanel2.setLayout(new BoxLayout(Ypanel2, BoxLayout.Y_AXIS));

                JPanel leftPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel exo2 = new JLabel("2- Générer tous les mots du langage Mir(Lk) = {w dans Mir(L(G)) / |w| ≤ k} pour k ≥ 0:");
                exo2.setForeground(new Color(0,100,0));
                JLabel exo2msg = new JLabel("Entrer la valeur du k = ");
                JTextField exo2k = new JTextField(20);
                exo2k.setPreferredSize(new Dimension(20, 20));
                JButton exe2Button = new JButton("EXE");
                JTextArea exe2Print = new JTextArea();
                exe2Print.setLineWrap(true); // Enable line wrapping
                exe2Print.setWrapStyleWord(true);
                
                exe2Button.addActionListener(e -> {
                    String textInt = exo2k.getText();
                    try {
                        int k = Integer.parseInt(textInt); // Parse the string to an integer
                        if (k < 0) {
                            exe2Print.setText("la langueur d'un mot n'est jamais moin que 0!");
                            exe2Print.setForeground(Color.red); 
                        }
                        else {
                            List<String> L2 = GeneratorR(k);
                            L2.set(0, index);
                            exe2Print.setText(L2.toString());                            
                        }
                    } catch (NumberFormatException ex) {
                        exe2Print.setText("Invalid input"); // Display an error message for invalid input
                    }                    
                });
                
                JScrollPane scrollPane2 = new JScrollPane(exe2Print);
                scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane2.setPreferredSize(new Dimension(100, 100)); // Set preferred size for scroll pane

                Ypanel2.add(Box.createVerticalStrut(20));
                Ypanel2.add(exo2);
                
                leftPanel2.add(exo2msg);
                leftPanel2.add(exo2k);
                leftPanel2.add(exe2Button);

                Ypanel2.add(leftPanel2);
                Ypanel2.add(scrollPane2);
                Ypanel.add(Ypanel2);



                JPanel Ypanel3 = new JPanel();
                Ypanel3.setLayout(new BoxLayout(Ypanel3, BoxLayout.Y_AXIS));

                JPanel leftPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftPanel3.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel exo3 = new JLabel("3- Générer tous les mots du langage ((Lk) puissance n) avec (k,n >= 0):");
                exo3.setForeground(new Color(0,100,0));
                JLabel exo3msgk = new JLabel("Entrer la valeur du k = ");
                JTextField exo3k = new JTextField(20);
                JLabel exo3msgn = new JLabel("                   Entrer la valeur de n (la puissance) = ");
                JTextField exo3n = new JTextField(20);

                exo3k.setPreferredSize(new Dimension(20, 20));
                exo3n.setPreferredSize(new Dimension(20, 20));

                JButton exe3Button = new JButton("EXE");
                JTextArea exe3Print = new JTextArea();

                exe3Print.setLineWrap(true); // Enable line wrapping
                exe3Print.setWrapStyleWord(true);
                
                exe3Button.addActionListener(e -> {
                    String textIntk3 = exo3k.getText();
                    String textIntn3 = exo3n.getText();
                    try {
                        int k = Integer.parseInt(textIntk3); // Parse the string to an integer
                        int n = Integer.parseInt(textIntn3);
                        if (k < 0 && n < 0) exe3Print.setText("la langueur d'un mot n'est jamais moin que 0!\nla puissance d'un mot n'est jamais moin que 0!");
                        else if (k < 0) exe3Print.setText("la langueur d'un mot n'est jamais moin que 0!");
                        else if (n < 0) exe3Print.setText("la puissance d'un mot n'est jamais moin que 0!");
                        else  {
                            List<String> L3 = GeneratorKn(k, n);
                            L3.set(0, index);
                            exe3Print.setText(L3.toString());
                        }
                        if (k < 0 || n < 0) exe3Print.setForeground(Color.red); 
                        else exe3Print.setForeground(Color.black);
                    } catch (NumberFormatException ex) {
                        exe3Print.setText("Invalid input"); // Display an error message for invalid input
                    }                    
                });
                
                JScrollPane scrollPane3 = new JScrollPane(exe3Print);
                scrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane3.setPreferredSize(new Dimension(100, 100)); // Set preferred size for scroll pane

                Ypanel3.add(Box.createVerticalStrut(20));
                Ypanel3.add(exo3);
                
                leftPanel3.add(exo3msgk);
                leftPanel3.add(exo3k);
                leftPanel3.add(exo3msgn);
                leftPanel3.add(exo3n);
                leftPanel3.add(exe3Button);

                Ypanel3.add(leftPanel3);
                Ypanel3.add(scrollPane3);
                Ypanel.add(Ypanel3);



                JPanel YpanelAnaliseur = new JPanel();
                YpanelAnaliseur.setLayout(new BoxLayout(YpanelAnaliseur, BoxLayout.Y_AXIS));

                JPanel leftPanelAnaliseur = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftPanelAnaliseur.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel leftAnsPile = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftAnsPile.setAlignmentX(Component.LEFT_ALIGNMENT);

                
                JPanel leftAnsSimple = new JPanel(new FlowLayout(FlowLayout.LEFT));
                leftAnsSimple.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel partie2 = new JLabel("\nPARTIE 2: ANALYSEUR SYNTAXIQUE:");
                partie2.setForeground(new Color(10, 10, 170));
                JLabel analiseur = new JLabel("Vérifie si un mot donné est syntaxiquement correct :");
                analiseur.setForeground(new Color(0,100,0));
                JLabel analiseurRemarque = new JLabel("On supposera que le mot donné est lexicalement correct!");
                JLabel analiseurmsg = new JLabel("Enter le mot:");
                JTextField analiseurMot = new JTextField(20);
                analiseurMot.setPreferredSize(new Dimension(20, 20));

                JButton analiseurButtonPile = new JButton("MODE AUTOMATE A PILE");
                JButton analiseurButtonSimple = new JButton("MODE SIMPLE");
                JLabel analiseurPrintPileMsg = new JLabel(  "1- On utilisons un Automate a Pile : ");
                JLabel analiseurPrintSimpleMsg = new JLabel("2- On utilisons la méthode simple :  "); //(génére le langage avec k = la longueur et voix s'il exist)
                JLabel[] leMotEst = {new JLabel("Le mot est syntaxiquement"), new JLabel("Le mot est syntaxiquement")};
                JLabel analiseurPrintPile = new JLabel();
                JLabel analiseurPrintSimple = new JLabel();
                analiseurButtonPile.addActionListener(e -> {
                    String mot = analiseurMot.getText();
                    try {
                        if (analiseurSyntaxiqueAutomatePile(mot)) {
                            analiseurPrintPile.setText(" CORRECT");
                            analiseurPrintPile.setForeground(new Color(0, 175, 0));
                        }
                        else {
                            analiseurPrintPile.setText(" INCORRECT");
                            analiseurPrintPile.setForeground(Color.red);
                        }
                    } catch (NumberFormatException ex) {
                        analiseurPrintPile.setText("Invalid input"); // Display an error message for invalid input
                    }            
                });

                analiseurButtonSimple.addActionListener(e -> {
                    String mot = analiseurMot.getText();
                    try {
                        if (analiseurSyntaxiqueSimple(mot)) {
                            analiseurPrintSimple.setText(" CORRECT");
                            analiseurPrintSimple.setForeground(new Color(0, 175, 0));
                        }
                        else {
                            analiseurPrintSimple.setText(" INCORRECT");
                            analiseurPrintSimple.setForeground(Color.red);
                        }
                    } catch (NumberFormatException ex) {
                        analiseurPrintSimple.setText("Invalid input"); // Display an error message for invalid input
                    }    
                });
                
                YpanelAnaliseur.add(Box.createVerticalStrut(20));
                YpanelAnaliseur.add(partie2);
                YpanelAnaliseur.add(Box.createVerticalStrut(10));
                YpanelAnaliseur.add(analiseur);
                YpanelAnaliseur.add(Box.createVerticalStrut(10));
                YpanelAnaliseur.add(analiseurRemarque);

                leftPanelAnaliseur.add(analiseurmsg);
                leftPanelAnaliseur.add(analiseurMot);
                leftPanelAnaliseur.add(analiseurButtonPile);
                leftPanelAnaliseur.add(analiseurButtonSimple);

                YpanelAnaliseur.add(leftPanelAnaliseur);

                leftAnsPile.add(analiseurPrintPileMsg);
                leftAnsPile.add(leMotEst[0]);
                leftAnsPile.add(analiseurPrintPile);
                YpanelAnaliseur.add(leftAnsPile);

                leftAnsSimple.add(analiseurPrintSimpleMsg);
                leftAnsSimple.add(leMotEst[1]);
                leftAnsSimple.add(analiseurPrintSimple);  
                YpanelAnaliseur.add(leftAnsSimple);              
                
                Ypanel.add(YpanelAnaliseur);
                panel.add(Ypanel);

                JScrollPane scrollPane = new JScrollPane(panel);
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                scrollPane.setPreferredSize(new Dimension(1000, 740));
                
                frame.getContentPane().add(scrollPane);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
        });
    }
}
