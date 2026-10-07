# =====================================================================
# TP -- Codage correcteur d'erreurs
# Communications Radiofrequences -- S7
# =====================================================================
# Squelette de code. Les fonctions/blocs marques
#     ### A COMPLETER ###
# sont a votre charge. Le reste est fourni pour vous faire gagner du
# temps. Le decoupage en parties correspond exactement a celui du
# sujet de TP (tp_codage_correcteur.pdf).
#
# Modulation utilisee : 4-QAM (= QPSK), 2 bits par symbole complexe.
#
# Dependances : numpy, scipy, matplotlib
#   pip install numpy scipy matplotlib
# =====================================================================

import numpy as np
import matplotlib.pyplot as plt
from scipy.special import erfc

np.random.seed(0)  # reproductibilite des resultats (a retirer si besoin)

BITS_PER_SYMBOL = 2  # 4-QAM : 2 bits par symbole (I et Q)


# =====================================================================
# PARTIE 1 -- Chaine de reference (4-QAM + canal AWGN)
# =====================================================================

def qam4_modulate(bits):
    """Mappage de Gray 4-QAM (= QPSK) : 2 bits -> 1 symbole complexe.
    bits : tableau de 0/1, de longueur PAIRE (paires (b_I, b_Q)).
    Chaque bit vaut 0 -> -1/sqrt(2), 1 -> +1/sqrt(2) sur son axe
    (I ou Q), de sorte que l'energie moyenne par symbole vaut 1
    (|s|^2 = 1/2 + 1/2 = 1)."""
    bits = bits.reshape(-1, 2)
    I = 2 * bits[:, 0] - 1
    Q = 2 * bits[:, 1] - 1
    return (I + 1j * Q) / np.sqrt(2)


def qam4_demodulate(received):
    """Decision dure independante sur I et sur Q (seuil 0).
    retourne un tableau de 0/1 de longueur 2*len(received),
    bits I et Q entrelaces : [b_I0, b_Q0, b_I1, b_Q1, ...]"""
    bI = (received.real > 0).astype(int)
    bQ = (received.imag > 0).astype(int)
    bits = np.empty(2 * len(received), dtype=int)
    bits[0::2] = bI
    bits[1::2] = bQ
    return bits


def awgn_channel_qam(symbols, EbN0_dB, R=1.0, bits_per_symbol=BITS_PER_SYMBOL):
    """Ajoute un bruit blanc gaussien complexe calibre a partir de Eb/N0.

    R = rendement du code (1.0 si non code).
    bits_per_symbol = nombre de bits transportes par symbole (2 pour
    la 4-QAM). A Eb/N0 fixe (energie par bit D'INFORMATION), l'energie
    disponible par symbole vaut :
        Es = bits_per_symbol * R * Eb
    (avec Es = 1 pour un symbole non code en 4-QAM, bits_per_symbol=2).
    Voir la remarque "Point de vigilance" du sujet de TP, Partie 2,
    et le Tableau des rappels express.
    """
    EbN0 = 10 ** (EbN0_dB / 10)
    N0 = 1 / (EbN0 * R * bits_per_symbol)   # avec Es = 1
    noise = np.sqrt(N0 / 2) * (np.random.randn(*symbols.shape)
                                + 1j * np.random.randn(*symbols.shape))
    return symbols + noise


def _pad_even(bits):
    """Ajoute un zero de bourrage si bits a une longueur impaire
    (necessaire car la 4-QAM regroupe les bits par paires).
    retourne (bits_pad, a_ete_bourre)."""
    if len(bits) % 2 == 1:
        return np.append(bits, 0), True
    return bits, False


def qam4_transmit(coded_bits, EbN0_dB, R):
    """Chaine complete modulation -> canal AWGN -> demodulation pour
    un train de bits CODES (deja encodes si applicable). Gere le
    bourrage eventuel a une longueur paire. A REUTILISER TELLE QUELLE
    dans ber_repetition et ber_hamming (Parties 2 et 3) : il n'y a
    aucune raison de reecrire la modulation/le canal a chaque fois."""
    padded, was_padded = _pad_even(coded_bits)
    symbols = qam4_modulate(padded)
    rx = awgn_channel_qam(symbols, EbN0_dB, R=R)
    bits_hat = qam4_demodulate(rx)
    if was_padded:
        bits_hat = bits_hat[:-1]
    return bits_hat


def ber_non_code(EbN0_range, n_bits=200000):
    ber = []
    for EbN0_dB in EbN0_range:
        bits = np.random.randint(0, 2, n_bits)
        bits_hat = qam4_transmit(bits, EbN0_dB, R=1.0)
        ber.append(np.mean(bits_hat != bits))
    return np.array(ber)


def _partie1_checks():
    """A executer avant la simulation complete pour verifier vos
    briques de base (aucune modification necessaire ici)."""
    print("=== Partie 1 : verifications ===")
    b = np.array([0, 1, 1, 0])
    s = qam4_modulate(b)
    print("bits            :", b)
    print("qam4_modulate   :", s)
    # attendu : [-0.707+0.707j  0.707-0.707j]
    # Sans bruit (EbN0 tres eleve), la demodulation doit être parfaite
    rx = awgn_channel_qam(s, EbN0_dB=40, R=1.0)
    print("demod (fort SNR):", qam4_demodulate(rx), "(attendu :", b, ")")
    print()
    # --------------------------------------------------------------
    EbN0_range = np.arange(0, 11, 1)
    ber = ber_non_code(EbN0_range)

    EbN0_linear = 10**(EbN0_range / 10)
    ber_theorique = 0.5 * erfc(np.sqrt(EbN0_linear))

    # Tracé
    plt.figure()
    plt.semilogy(EbN0_range, ber, 'o-', label='BER simulée')
    plt.semilogy(EbN0_range, ber_theorique, 'r--', label='BER théorique') 

    #   La courbe simulé ne colle pas tout le long de la courbe notamment sur la fin ou les courbes se divisent 
    
    plt.xlabel(r'$E_b/N_0$ (dB)')
    plt.ylabel('BER')
    plt.title('BER en fonction de $E_b/N_0$')
    plt.grid(True, which='both')
    plt.legend()
    plt.show()


# =====================================================================
# PARTIE 2 -- Code de repetition (3,1)
# =====================================================================
# Rappel : R = 1/3, d_min = 3, t = 1 erreur corrigible.

def repetition_encode(bits):
    """Chaque bit d'information est repete 3 fois.
    bits : tableau de 0/1 de longueur k
    retourne : tableau de 0/1 de longueur 3k"""
    tab = np.repeat(bits,3)
    return tab


def repetition_decode(bits3):
    """bits3 : tableau de 0/1 de longueur 3k (bruite, apres
    demodulation dure). Decode par vote majoritaire.
    retourne : tableau de 0/1 de longueur k"""
    groupes = bits3.reshape(-1, 3)
    return (np.sum(groupes, axis=1) >= 2).astype(int)



def ber_repetition(EbN0_range, n_bits=60000):
    """Sur le modele de ber_non_code, mais :
      - encoder les bits (repetition_encode)
      - transmettre avec qam4_transmit(..., R=1/3)  (facteur de
        rendement ! reutilisez la fonction fournie en Partie 1)
      - decoder (repetition_decode)
      - comparer aux bits D'INFORMATION d'origine (longueur n_bits)
    """
    ber = []
    
    for EbN0_dB in EbN0_range:
        # Bits d'information
        bits = np.random.randint(0, 2, n_bits)
        # Codage par répétition x3
        bits3 = repetition_encode(bits)
        # Transmission avec rendement R = 1/3
        bits3_hat = qam4_transmit(bits3, EbN0_dB, R=1/3)
        # Décodage par vote majoritaire
        bits_hat = repetition_decode(bits3_hat)
        # BER par rapport aux bits d'information
        ber.append(np.mean(bits_hat != bits))
    return np.array(ber)



def _partie2_checks():
    """A executer avant la simulation complete."""
    print("=== Partie 2 : verifications ===")
    b = np.array([1, 0, 1, 1])
    c = repetition_encode(b)
    print("bits           :", b)
    print("encode (3k)    :", c)
    # attendu :
    # [1 1 1 0 0 0 1 1 1 1 1 1]

    # Une seule erreur
    c_bruite = c.copy()
    c_bruite[0] = 1 - c_bruite[0]
    d = repetition_decode(c_bruite)
    print("avec 1 erreur  :", d)
    print("(attendu :", b, ")")
    print()

    # --------------------------------------------------
    # Simulation
    # --------------------------------------------------
    EbN0_range = np.arange(0, 11, 1)
    # Ber Non codé
    ber_noncode = ber_non_code(EbN0_range)
    # Ber Répétition (3,1)
    ber_rep = ber_repetition(EbN0_range)

    # --------------------------------------------------
    # Tracé
    # --------------------------------------------------
    plt.figure()
    plt.semilogy(EbN0_range,ber_noncode,'o-',label='Non codé')
    plt.semilogy(EbN0_range,ber_rep,'s-',label='Répétition (3,1)')
    plt.xlabel(r'$E_b/N_0$ (dB)')
    plt.ylabel('BER')
    plt.title('Non codé vs code de répétition (3,1)')
    plt.grid(True, which='both')
    plt.legend()
    plt.show()


# =====================================================================
# PARTIE 3 -- Code de Hamming (7,4)
# =====================================================================
# Rappel (forme systematique G = [I4 | P], H = [P^T | I3]) :
#   R = 4/7, d_min = 3, t = 1 erreur corrigible.

G = np.array([
    [1, 0, 0, 0, 1, 1, 0],
    [0, 1, 0, 0, 1, 1, 1],
    [0, 0, 1, 0, 1, 0, 1],
    [0, 0, 0, 1, 0, 1, 1],
])

H = np.array([
    [1, 1, 1, 0, 1, 0, 0],
    [1, 1, 0, 1, 0, 1, 0],
    [0, 1, 1, 1, 0, 0, 1],
])


def hamming_encode(u4):
    """u4 : tableau de 0/1 de longueur multiple de 4.
    retourne : mots de code de longueur 7*len(u4)/4"""
    u4 = u4.reshape(-1, 4)
    c = (u4 @ G) % 2 # c=uG : on passe de 4 bits à 7 car les 3 derniers nous indique si on a une erreur
    # or R =1/3 avant et maintenant on a R=4/7

    return c.reshape(-1)


# table syndrome -> position d'erreur (colonnes de H)
# H[:, i] est le syndrome attendu si le bit i est errone
syndrome_table = {tuple(H[:, i]): i for i in range(7)}


def hamming_decode(r7):
    """r7 : mots recus (bruites, apres demodulation dure),
    longueur multiple de 7. Corrige au plus 1 erreur par mot
    de 7 bits puis extrait les 4 bits d'information."""
    
    r7 = r7.reshape(-1, 7).copy()

    for i in range(len(r7)):
        # Calcul du syndrome
        s = (r7[i] @ H.T) % 2 # s=rH**T -> donne le syndrome donc indique une erreursi différent de 0

        # Si le syndrome n'est pas nul, on cherche la position
        # de l'erreur dans la table
        if np.any(s):
            position = syndrome_table.get(tuple(s))

            # Si le syndrome correspond à une erreur simple
            if position is not None:
                r7[i, position] = 1 - r7[i, position]

    # Les 4 premiers bits sont les bits d'information
    return r7[:, :4].reshape(-1)

def ber_hamming(EbN0_range, n_bits=56000):
    """Sur le meme modele que ber_repetition, avec :
      - hamming_encode (regrouper les bits par paquets de 4)
      - qam4_transmit(..., R=4/7)
      - hamming_decode
    """
    # On s'assure que n_bits est un multiple de 4
    n_bits = (n_bits // 4) * 4
    ber = []
    for EbN0_dB in EbN0_range:
        # Bits d'information
        bits = np.random.randint(0, 2, n_bits)
        # Codage Hamming (4 bits -> 7 bits)
        coded_bits = hamming_encode(bits)
        # Transmission 4-QAM + canal AWGN
        # R = 4/7 car 4 bits d'information
        # sont transmis sous forme de 7 bits codés
        received_bits = qam4_transmit(
            coded_bits,
            EbN0_dB,
            R=4/7
        )
        # Décodage Hamming
        bits_hat = hamming_decode(received_bits)
        # BER par rapport aux bits d'information
        ber.append(np.mean(bits_hat != bits))
    return np.array(ber)


def _partie3_checks():
    """Reproduit l'exemple numerique du cours :
    u = (1,0,1,1), erreur sur le 5e bit."""
    print("=== Partie 3 : verifications ===")
    u = np.array([1, 0, 1, 1])
    c = hamming_encode(u)
    print("u = (1,0,1,1)  -> c =", c)  # attendu : [1 0 1 1 0 0 0]

    r = c.copy()
    r[4] = 1 - r[4]  # on force une erreur sur le 5e bit (indice 4)
    print("recu (erreur pos. 5) :", r)

    d = hamming_decode(r)
    print("decode              :", d, "(attendu :", u, ")")

    # et avec DEUX erreurs simultanees ?
    r2 = c.copy()
    r2[1] = 1 - r2[1]
    r2[4] = 1 - r2[4]
    d2 = hamming_decode(r2)
    print("avec 2 erreurs       :", d2, "(la correction peut echouer, "
          "c'est normal : t=1 seulement)")
    print()


# =====================================================================
# PARTIE 4 -- Hamming (15,11), a comparer au Hamming (7,4)
# =====================================================================
# Construction systematique d'un code de Hamming(15,11) : meme principe
# que pour Hamming(7,4) (Partie 3), avec cette fois 4 bits de parite
# (au lieu de 3) et 11 bits d'information (au lieu de 4).
# R = 11/15 (a comparer a R = 4/7 pour le (7,4)), d_min = 3, t = 1.
#
# La construction de G15/H15 est FOURNIE (elle n'est pas l'objet de ce
# bonus) : le but est de comparer les performances au code de Hamming
# (7,4) deja etudie en Partie 3.

import itertools

_all_nonzero_4 = [np.array(v) for v in itertools.product([0, 1], repeat=4) if any(v)]
_weight1 = [v for v in _all_nonzero_4 if v.sum() == 1]   # deviennent les
                                                          # colonnes de I4
_others  = [v for v in _all_nonzero_4 if v.sum() >= 2]   # 11 vecteurs
                                                          # restants (donnees)

P15   = np.array(_others)                             # 11 x 4
G15   = np.hstack([np.eye(11, dtype=int), P15])        # 11 x 15
H15_T = np.vstack([P15, np.eye(4, dtype=int)])         # 15 x 4  (= H15^T)
H15   = H15_T.T                                        # 4 x 15

syndrome_table15 = {tuple(H15[:, i]): i for i in range(15)}


def hamming15_encode(u11):
    """u11 : tableau de 0/1 de longueur multiple de 11."""
    u11 = u11.reshape(-1, 11)
    return ((u11 @ G15) % 2).reshape(-1)


def hamming15_decode(r15):
    """Meme principe que hamming_decode (Partie 3), mais sur des mots
    de 15 bits (11 d'information + 4 de parite)."""
    r15 = r15.reshape(-1, 15).copy()
    for row in r15:
        s = tuple((row @ H15_T) % 2)
        if any(s):
            pos = syndrome_table15.get(s)
            if pos is not None:
                row[pos] = 1 - row[pos]
    return r15[:, :11].reshape(-1)


def ber_hamming15(EbN0_range, n_bits=110000):
    """A COMPLETER, sur le meme modele que ber_hamming (Partie 3) :
      - regrouper les bits par paquets de 11 (n_bits doit etre un
        multiple de 11 -- ajustez si besoin)
      - hamming15_encode, qam4_transmit(..., R=11/15), hamming15_decode
    """
    # On s'assure que n_bits est un multiple de 11
    n_bits = (n_bits // 11) * 11
    ber = []
    for EbN0_dB in EbN0_range:
        # Bits d'information
        bits = np.random.randint(0, 2, n_bits)
        # Codage Hamming (11 bits -> 15 bits)
        coded_bits = hamming15_encode(bits)
        # Transmission 4-QAM + canal AWGN
        # R = 11/15 car 11 bits d'information
        # sont transmis sous forme de 15 bits codés
        received_bits = qam4_transmit(
            coded_bits,
            EbN0_dB,
            R=11/15
        )
        # Décodage Hamming
        bits_hat = hamming15_decode(received_bits)
        # BER par rapport aux bits d'information
        ber.append(np.mean(bits_hat != bits))
    return np.array(ber)


def _partie4_checks():
    print("=== Partie 6 (bonus) : verifications ===")
    u = np.array([1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0])  # 11 bits
    c = hamming15_encode(u)
    print("u (11 bits)     :", u)
    print("c = encode(u)   :", c)
    r = c.copy()
    r[5] = 1 - r[5]
    d = hamming15_decode(r)
    print("decode(1 erreur):", d, "(attendu :", u, ")")
    print()


def partie4_comparaison(EbN0_range=None):
    """Superpose non code, Hamming(7,4) et Hamming(15,11) : a executer
    une fois ber_hamming15 complete."""
    if EbN0_range is None:
        EbN0_range = np.arange(0, 11, 1)

    ber_nc = ber_non_code(EbN0_range)
    ber_h7 = ber_hamming(EbN0_range)
    ber_h15 = ber_hamming15(EbN0_range)

    plt.figure()
    plt.semilogy(EbN0_range, ber_nc, 'o-', label='Non code')
    plt.semilogy(EbN0_range, ber_h7, '^-', label='Hamming (7,4)')
    plt.semilogy(EbN0_range, ber_h15, 'v-', label='Hamming (15,11)')
    plt.xlabel('Eb/N0 (dB)')
    plt.ylabel('BER')
    plt.legend()
    plt.grid(True, which='both')
    plt.title('Bonus : Hamming (7,4) vs Hamming (15,11)')
    plt.show()

    return ber_nc, ber_h7, ber_h15

# =====================================================================
# PARTIE 5 -- Synthese comparative
# =====================================================================

def partie4_synthese(EbN0_range=None):
    if EbN0_range is None:
        EbN0_range = np.arange(0, 11, 1)

    ber_nc = ber_non_code(EbN0_range)
    ber_rep = ber_repetition(EbN0_range)
    ber_ham = ber_hamming(EbN0_range)
    ber_ham15 = ber_hamming15(EbN0_range)

    plt.figure()
    plt.semilogy(EbN0_range, ber_nc, 'o-', label='Non code')
    plt.semilogy(EbN0_range, ber_rep, 's-', label='Repetition (3,1)')
    plt.semilogy(EbN0_range, ber_ham, '^-', label='Hamming (7,4)')
    #--------------------------------
    #ajout de la courbe Hamming (15,11)
    plt.semilogy(EbN0_range, ber_ham15, 'v-', label='Hamming (15,11)')
    #--------------------------------

    plt.xlabel('Eb/N0 (dB)')
    plt.ylabel('BER')
    plt.legend()
    plt.grid(True, which='both')
    plt.title('Comparaison des codes correcteurs (4-QAM)')
    plt.show()

    return ber_nc, ber_rep, ber_ham, ber_ham15
    
# =====================================================================
# Point d'entree
# =====================================================================
if __name__ == "__main__":
    # Etape 1 : verifications unitaires (a faire AVANT la simulation
    # complete -- decommentez au fur et a mesure que vous completez
    # chaque partie)
    _partie1_checks()

    """ Partie 2 Réponses
    1/ 
    Dans la simulationla courbe du code de répétition ne devient pas meiulleure que la courbe non codéedans la simulation
    En effet comme dans la répétition on a R=1/3 on est forcément moins efficace que la courbe non codée mais on est plus résistant au bruit
    Par conséquent c'est normal que nos courbes théorique soient dans cette configuration
    
    2/
    Si on maintient un débit symbole fixe, l'impact du code de répétition sur le débit utile 
    provoque une forte diminution de quantité d'information transmise.

    A l'inverse, en conservant le débit utile constant, la bande passante devra de ce fait fortement augmenter avec un facteur 3.
    """
    
    #_partie2_checks()

    """ Partie 3 Réponses

    1/ A Ber 10e-3 on observe le ber hamming à 7dB ce qui est étonnament différent de ce à quoi je m'attendais.
    Etant donné le rapport R=4/7 j'imaginais que la rentabilité du code hamming serais entre la courbe non codé et la courbe à répétition.
    
    2/ R= k/n=4/7  Car on a k=4 bits d'information en entrée, les 3 autres représentant une 'redondance' différente de celle du code à répétition

    3/Comme le code ne peut corriger qu'une seule erreur (t=1), le syndrome résultant de deux erreurs peut être interprété à tort comme celui d'une erreur simple.

    """
    #_partie3_checks()

    """ Partie 4 Hamming (15,11)

    1/ Le code de hamming (15,11) apparaît être plus avantageux que le code de hamming (7,4) pour la simple 
    et bonne raison que plus de bits d'information sont transmis (4/7)>(11/15).
    2/ l'écart de performance est clairement visible avec la différence de dB observable sur le graphique (ici de environ 1dB)
    3/ Il s'agit d'un compromis et celon la situation il nous faut plus de transmision et d'autres fois plus de fiabilité, l'écart Rendement/Fiabilité sont interdépendant .
    """
    # _partie6_checks()

    # Etape 2 : simulation complete + graphique de synthese
    # (decommentez une fois les trois parties ci-dessus completees)
    #partie4_synthese()

    """ Résultats du tableau de synthèse :
    Code :          R       dmin    t       Gc mesuré à 10e-3 (dB)
    Non Codé        1       1       0       7

    Répétition      1/3     3       1       8.1
    
    Hamming (7,4)   4/7     3       1       7
    
    Hamming(15,11)  11/15   3       1       6
    """


    # Etape 3 (bonus) : comparaison Hamming(7,4) vs Hamming(15,11)
    #partie4_comparaison()

    """ Comparaison de Hamming (7,4) et Répétition :
    Au vu de la différence de R=1/3 e R=4/7, il y a un compromis entre la correction d'erreurs et la redondance qui a un impact sur le rendu des courbes.
    Le Hamming utilise plus efficacement la redondance : il obtient la même capacité de correction avec moins de surcharge, 
    ce qui explique la différence entre les deux courbes.
    """