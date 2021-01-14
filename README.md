## Flash-storage

>This is a tutorial project, check the code before using!



![Flash-storage screen](https://github.com/AlonaZabluda/Flash-storage/blob/main/img/GH.png)

## Introduction

**_Flash-storage_** - is a client-server application, similar to DropBox and Google Drive, with the ability to send files to the server, view the list of files, download files from server and some other features.

[Video-demonstration](https://monosnap.com/file/3sCC0dSXCEvMjirFSdhrjJQKx4bhHE) of **_Flash-storage_** functions.

## Features

- user authorization using the DB;
- ability to register new users;
- uploading a file to a PC or Server;
- deleting files;
- renaming files;
- go up/down the folder tree;
- each user on the server has his own directory accessible only to him for all available functions in the app;
- each user can open access to his directory to other users, but in a limited mode (only downloading files to their PC`s).

## Building

To deploy database from generated sql file I attached link to [mysqldump](https://drive.google.com/file/d/1UE1X5vbQSxBYgq7oyX0ZgkJWY8-oPT8l/view?usp=sharing).


## Supplement

- **Netty** - was used for networking;
- **JavaFX** - the graphical part was done using this software platform;
- **MySQL** - to store info about users and open access to directories;
- **Log4j** - logging utility;
- **Maven** - build system.


