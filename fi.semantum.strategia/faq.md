Usein Kysytyt Kysymykset
------------

1) Kartan elementtejä kirjoittaessa näkyy välillä ehdotusvalikko, mikä se on?
2) Miksi visio-laatikko ei näy halutulla tasolla?
3) Miten lukujen automaattinen taulukoiden generointi toimii?
4) Kartan laatikon otsikossa ei näy tason vuotta, saako sen näkyviin?
5) Muutosehdotus ei näy toiselle virastolle, ellei tätä virastoa ole pyydetty ottamaan muutosehdotukseen kantaa. Miksi?
6) Vasta kommentoinnin aikana voi tulla tarve pyytää toisen viraston kantaa. Onko tämä mahdollista?
7) Näkyvätkö kommentit automaattisesti osastojen tunnuksilla?
8) Uuden muutosehdotuksen luonti on vaikeaa, sillä vaadittavia kenttiä on niin monta. Miksi?
9) Miten tulossopimuksen lukujen poistaminen/lisääminen onnistuu?
10) Mikä on etusivun muokkauksessa Linkit-toiminto?
11) Painopistekoonti-näkymässä toimintovalikon toiminnot eivät saatavilla? Esim. Lisää alakartta/yläkartta. virastolaatikossa Lisää-painopiste, yms.
12) Kartan navigointiopas
13) Tulossopimusten kirjoitus- ja lukuoikeudet

### 1) Kartan elementtejä kirjoittaessa näkyy välillä ehdotusvalikko, mikä se on?

Joissain selaimissa on sisäänrakennetu ominaisuus, joka tallentaa sivulle annettujen kenttien arvoja ja ehdottaa niitä automaattisesti. Tämä toiminnallisuus tulee siis täysin sovelluksen ulkopuolelta.

### 2) Miksi visio-laatikko näkyy/ei näy halutulla tasolla?

Jos karttatyyppi määrittää vision, visio-laatikko näkyy tyypin tasolla. Muokkaamalla kartan tyyppiä, voi visio-laatikkon piilottaa kokonaan. Jos karttatyyppi määrittää vision, mutta visiota ei ole annetta, tulee visio-laatikkon tekstiksi "(ei visiota)". Jos siis haluaa visio-laatikko kokonaan piiloon täytyy muokata kartan tyyppiä "Kartta määrittää vision" kohdan tyhjäksi. Kartan tyypin voi muokata:

Syöttötila -> Kartan Tyyppi -> Muokkaa -> Kartta määrittää vision (kyllä/ei valinta) -> Tee muutokset.

### 3) Miten lukujen automaattinen taulukoiden generointi toimii?

Sovelluksessa on määritelty muutama tunniste, joka automaattisesti lukee kartan tietokantaa ja generoi taulukon tulossopimuksiin. Tunnisteita voi laittaa mihin tahansa kappaleeseen, mihin tahansa kohtaa tekstiä. Näet kartan kaikki tunnisteet painamalla "? Apu" näppäintä Tulossopimus näkymässä (nappi on Tulossopimusnäkymän oikeassa kulmassa, vain näkyvillä heille joilla on kirjoitusoikeus Tulossopimuksiin). Seuraa Tulossopimusnäkymän "? Apu" ikkunan ohjeita.

### 4) Kartan laatikon otsikossa ei näy tason vuotta, saako sen näkyviin?

Valitettavasti karttasovelluksen otsikkoihin ei ole tuotu laatikon vuotta. Oletuksena kartassa vaihdessava vuosi-filtteri on indikaattori sille, mihin vuoteen tietty laatikko liittyy. Jos karttaa siis navigoi "2019" vuosi-filtteri päällä, kaikki laatikot, jotka näkyvät, liittyvät vuoteen "2019".

### 5) Muutosehdotus ei näy toiselle virastolle, ellei tätä virastoa ole pyydetty ottamaan muutosehdotukseen kantaa. Miksi?

Muutosehdotukset ovat lähtökohtaisesti näkyvissä vain niille virastoille, joita asia koskee. Kun virasto luo muutosehdotuksen, vain Tulosohjaajat liitetään osaksi muutosehdotuksen hyväksymisprosessia.

Jos käyttäjäryhmälle haluaa lisätä näkyvyyden kaikkiin muutosehdotuksiin, täytyy System/Admin käyttäjän muokata ryhmää "Hallinnoi" näkymästä ja lisätä tälle luku-oikeus koko kommenttityökalulle. Muutoin käyttäjäryhmä näkee vain ne muutosehdotukset, jotka oma virasto on tehnyt, tai jotka vaativat viraston kannanottoa.

### 6) Vasta kommentoinnin aikana voi tulla tarve pyytää toisen viraston kantaa. Onko tämä mahdollista?

On. Virastoa voi pyytää ottamaan kantaa muutosehdotukselle niin kauan kuin sitä ei ole suljettu tai siirretty tulosneuvotteluihin.

### 7) Näkyvätkö kommentit automaattisesti osastojen tunnuksilla?

Kommentit näkyvät muutosehdotuksen alla niille, joilla on luku oikeus muutosehotukselle. Muutosehdotukset näkyvät automaattisesti käyttäjälle, joka on osa virastoa jolla on näkyvyys muutosehdotukselle. System/Admin käyttäjä pystyy näkemään mihin virastoon käyttäjä kuulluu ja voi muokata tätä tarvittaessa.

### 8) Uuden muutosehdotuksen luonti on vaikeaa, sillä vaadittavia kenttiä on niin monta. Miksi?

Muutosehdotukselle on pakko antaa riittävästi tietoa, jotta kaikki osapuolet ymmärtävät mistä on kyse.

### 9) Miten tulossopimuksen lukujen poistaminen/lisääminen onnistuu?

Tulossopimuksen luvut määritellään sovelluksen käynnistysvaiheessa, eli palvelimen asennusvaiheessa. Tämä määrittää koko tietokannan rakenteen eikä tällä hetkellä ole vaihdettavissa jälkikäteen. Asennusvaiheessa täytyy siis ottaa kantaa siihen, mitä lukuja tulossopimukseen halutaan.

### 10) Mikä on etusivun muokkauksessa Linkit-toiminto?

System/Admin käyttäjä, joka voi muokata pääsivua, näkee linkit toiminnon muokatessaan etusivua. Jos etusivun vasempaan yläkulmaan halutaan linkki jollekin sivulle, esim. ohjevideolle, tai organisaation pääsivulle, tämä on hyvä kohta. System/Admin käyttäjän tulee täyttää "Linkki:" kenttään https:// alkuinen osoite toiselle sivulle ja antaa sille ymmärrettävä nimi "Nimi:" kentällä. Jos linkkiä ei anneta, mitään ei näy etusivun vasemmassa yläkulmassa.

### 11) Painopistekoonti-näkymässä toimintovalikon toiminnot eivät saatavilla? Esim. Lisää alakartta/yläkartta. virastolaatikossa Lisää-painopiste, yms.

Koonti-näkymät ovat vain luettavissa olevia näkymiä, joissa ei voi muokata karttaa. Siksi toimintovalikko on tyhjä. Tavallista karttaa muokattaessa koontinäkymien sisältäkin päivittyy automaattisesti.

### 12) Kartan navigointiopas

Otsikkoalue näyttää sekä nykyisen kartan nimen että reitin jota pitkin karttaan on päästy ylemmän tason karttojen kautta.

Voit siirtyä ylemmän tason karttoihin klikkaamalla karttojen nimiä.

Voit muuttaa kartan otsikkoa klikkaamalla sitä mikäli olet syöttötilassa ja tunnuksellasi on muutosoikeus tähän karttaan.

Jos olet syöttötilassa ja sinulla on kartan muutosoikeus: Voit muuttaa kartan otsikkoa klikkaamalla sitä.
Jos olet syöttötilassa, mutta tunnuksellasi ei ole tähän karttaan muutosoikeutta: Voit pyytää muutosoikeutta kartan ylläpitäjältä.

Jos olet katselutilassa: Voit muuttaa kartan otsikkoa siirtymällä syöttötilaan yläpalkin toiminnolla ja tämän jälkeen klikkaamalla kartan otsikkoa.
Jos olet katselutilassa eikä tunnuksellasi ole kartan muutosoikeutta: Syöttötilassa et voi tehdä mitään.

### 13) Tulossopimusten kirjoitus- ja lukuoikeudet

Jos käyttäjällä ei ole virastoa, eikä ole admin-oikeuksia sovelluksessa, he eivät näe mitään tulossopimuksia.
Jos käyttäjä tosin kuuluu virastoon, mutta heillä ei ole lisäoikeuksia, he pystyvät vain lukemaan oman viraston tulossopimuksen.

Jotta käyttäjä voi muokata tulossopimusta, heille täytyy antaa *Tulossopimuksen kirjoutusoikeus*. Tämän jälkeen he voivat vain muokata oman virastonsa tulossopimusta.

Nähdäkseen kaikkien virastojen tulossopmukset, käyttäjälle täytyy lisätä *Luku oikeus Tulossopimusnäkymän ylläpitäjiin*.

Jotta käyttäjä voi muokata kaikkien virastojen kaikkia tulossopimuksia, heidän täytyy kuulua *Tulossopimusnäkymän ylläpitäjiin Kirjoitus oikeudella*. Myös tavalliset admin käyttäjät voivat kirjoittaa kaikkiin tulossopimuksiin.