# cordova-plugin-uspeedsaf

Accès système de fichiers **Android** via *Storage Access Framework* et *Mediastore*.

Le code source initial provient des dépôts suivants :
- [sahusoftcom](https://github.com/sahusoftcom/cordova-documents-provider-read-write) : dépôt initial
- [customautosys](https://github.com/customautosys/cordova-plugin-saf-mediastore) : fork de **sahusoftcom**
- [Makiwin](https://github.com/Makiwin/cordova-plugin-saf-mediastore) : fork de **customautosys**

## Licence

Le dépôt GitHub initial de [sahusoftcom](https://github.com/sahusoftcom/cordova-documents-provider-read-write) était sous licence [Apache 2.0](./LICENSE.md).  
J'ai décidé de le laisser sous licence [Apache 2.0](./LICENSE.md) même si [customautosys](https://github.com/customautosys/cordova-plugin-saf-mediastore) s'est permis de la changer en cours de route.  

## Appels

Les appels se sont comme suit :
```ts
// returne une "Promise"
cordova.plugins.USpeedSaf.<function>(params);

// returne les données de la "Promise"
await cordova.plugins.USpeedSaf.<function>(params);
```

## Fonctions disponibles

- Lancement de l'interface native pour choisir un répertoire dans lequel pourrait être sauvegardé un fichier.
```ts
selectFolder(uri?: string): Promise<string>;
```

- Lancement de l'interface native pour choisir un fichier.
```ts
selectFile(uri?: string): Promise<string>;
```

- Lancement de l'interface native pour ouvrir un répertoire.
```ts
openFolder(uri: string): Promise<void>;
```

- Lancement de l'interface native pour ouvrir un fichier.
```ts
openFile(uri: string): Promise<void>;
```

- Lecture du contenu d'un fichier vers un `ArrayBuffer`.
```ts
readFile(uri: string): Promise<ArrayBuffer>;
```

- Écriture d'un fichier.
```ts
writeFile(params: USpeedSafWriteParameters): Promise<string>;
```

- Écrasement d'un fichier existant.
```ts
overwriteFile(params: USpeedSafOverwriteParameters): Promise<string>;
```

- Lancement de l'interface native pour écrire un fichier.
```ts
saveFile(params: USpeedSafSaveParameters): Promise<string>;
```

- Suppression d'un fichier.
```ts
deleteFile(uri: string): Promise<number>;
```

- Accès au nom de fichier à partir de l'URI fournie.
```ts
getFileName(uri: string): Promise<string>;
```

- Accès à l'URI d'un fichier dans le répertoire/sous-répertoire spécifié
```ts
getUri(params: USpeedSafGetUriParameters): Promise<string>;
```

- Vérification de l'existance d'un fichier dans le répertoire `Download`.
```ts
existsFile(filename: string): Promise<string>;
```

- Accès aux informations d'un fichier à partir d'une URI.
```ts
getDataForOpenFile(uri: string): Promise<USpeedSafDataForOpenResult>;
```

