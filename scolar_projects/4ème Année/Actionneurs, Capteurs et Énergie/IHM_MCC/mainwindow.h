#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QSerialPort>
#include <QSerialPortInfo>
#include <QComboBox>
#include <QPushButton>
#include <QLabel>
#include <QSpinBox>
#include <QCheckBox>
#include <QLCDNumber>

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private slots:
    void actualiserPorts();   // Détecte les ports série branchés
    void gererConnexion();    // Ouvre ou ferme la liaison série
    void demarrerMoteur();    // Envoie la consigne et le sens (Start)
    void arreterMoteur();     // Envoie l'ordre d'arrêt (Stop)
    void lireDonneesSerie();
    void arretUrgence();

private:
    // Périphérique série
    QSerialPort *serial;

    void mettreAJourStatut(bool connecte);
    QByteArray bufferSerie;

    QLabel *ledStatut;
    QLabel *lblStatutTexte;
    QPushButton *btn_urgence;

    // Composants graphiques dédiés à la connexion
    QComboBox *comboPorts;
    QPushButton *btnRefresh;
    QPushButton *btnConnect;

    QLabel *lbl_vitesse;
    QLabel *lbl_courant;

    QSpinBox *spinbox_moteur;
    QCheckBox *chkBox_sens;
    QPushButton *btn_start;
    QPushButton *btn_stop;

    QLCDNumber *lcd_vitesse;
    QLCDNumber *lcd_courant;

};

#endif // MAINWINDOW_H
