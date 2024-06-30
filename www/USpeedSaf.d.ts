/** Paramètres pour la fonction `writeFile`. */
type USpeedSafWriteParameters =
{
  /** Données à écrire (*en base 64*) */  data:       string;
  /** Nom de fichier cible */             filename:   string;
  /** Répertoire cible éventuel */        folder?:    string;
  /** Sous-répertoire cible éventuel */   subFolder?: string;
};

/** Paramètres pour la fonction `owerwriteFile`. */
type USpeedSafOverwriteParameters =
{
  /** L'URI du fichier cible */               uri:  string;
  /** Les données à écrire (*en base 64*) */  data: string;
};

/** Paramètres pour la fonction `saveFile`. */
type USpeedSafSaveParameters =
{
  /** Les données à écrire (*en base 64*) */  data:       string;
  /** Nom de fichier cible */                 filename?:  string;
  /** Répertoire cible éventuel */            folder?:    string;
};

/** Paramètres pour la fonction `getUri`. */
type USpeedSafGetUriParameters =
{
  /** Répertoire */       folder:     string;
  /** Sous-répertoire */  subFolder?: string;
  /** Nom de fichier */   filename?:  string;
};

/** Les données retournée par `getDataForOpenFile`. */
type USpeedSafDataForOpenResult =
{
  /** URI du fichier */   uri:      string;
  /** Type MIME */        mimeType: string;
  /** Nom de fichier */   filename: string;
};

/** Interface du plugin `USpeedSaf`. */
interface USpeedSaf
{
  /**
   * Lancement de l'interface native pour choisir un répertoire dans lequel pourrait être sauvegardé un fichier.
   * @param {string} uri l'eventuel répertoire initial
   * @return {Promise<string>} l'URI du répertoire choisi
   */
  selectFolder(uri?: string): Promise<string>;

  /**
   * Lancement de l'interface native pour choisir un fichier.
   * @param {string} uri l'eventuel répertoire initial
   * @return {Promise<string>} l'URI du fichier choisi
   */
  selectFile(uri?: string): Promise<string>;

  /**
   * Lancement de l'interface native pour ouvrir un répertoire.
   * @param {string} uri l'URI du répertoire
   * @return {Promise<void>} rien
   */
  openFolder(uri: string): Promise<void>;

  /**
   * Lancement de l'interface native pour ouvrir un fichier.
   * @param {string} uri file URI
   * @return {Promise<void>} the promise
   */
  openFile(uri: string): Promise<void>;

  /**
   * Lecture du contenu d'un fichier vers un `ArrayBuffer`.
   * @param {string} uri l'URI du fichier à lire
   * @return {Promise<ArrayBuffer>} les données binaires du fichier lu
   */
  readFile(uri: string): Promise<ArrayBuffer>;

  /**
   * Écriture d'un fichier.
   * Les données doivent être fournies en base 64.
   * Le fichier est créé, s'il n'existe pas, ou écrasé, s'il existe.
   * Le répertoire par défaut est `Downloads`.
   * @param {USpeedSafWriteParam} params le paramétrage
   * @return {Promise<string>} l'URI du fichier écrit
   */
  writeFile(params: USpeedSafWriteParameters): Promise<string>;

  /**
   * Écrasement d'un fichier existant.
   * @param {USpeedSafOverwriteParam} params les paramètres
   * @return {Promise<string>} l'URI du fichier écrit
   */
  overwriteFile(params: USpeedSafOverwriteParameters): Promise<string>;

  /**
   * Lancement de l'interface native pour écrire un fichier.
   * @param {USpeedSafSaveParam} params les paramètres
   * @return {Promise<string>} l'URI du fichier écrit
   */
  saveFile(params: USpeedSafSaveParameters): Promise<string>;

  /**
   * Suppression d'un fichier.
   * @param {string} uri l'URI du fichier à supprimer
   * @return {number} le nombre de fichiers supprimés
   */
  deleteFile(uri: string): Promise<number>;

  /**
   * Accès au nom de fichier à partir de l'URI fournie.
   * @param {string} uri l'URI d'un fichier
   * @return {Promise<string>} le nombre du fichier
   */
  getFileName(uri: string): Promise<string>;

  /**
   * Accès à l'URI d'un fichier dans le répertoire/sous-répertoire spécifié
   * @param {USpeedSafGetUriParam} params les paramètres
   */
  getUri(params: USpeedSafGetUriParameters): Promise<string>;

  /**
   * Vérification de l'existance d'un fichier dans le répertoire `Download`.
   * @param {string} filename le nom de fichier
   * @return {Promise<string>} l'URI du fichier
   */
  existsFile(filename: string): Promise<string>;

  /**
   * Accès aux informations d'un fichier à partir d'une URI.
   * @param {string} uri l'URI du fichier
   * @return {Promise<USpeedSafDataForOpenResult>} les informations du fichier
   */
  getDataForOpenFile(uri: string): Promise<USpeedSafDataForOpenResult>;
}

interface CordovaPlugins
{
  USpeedSaf: USpeedSaf;
}
