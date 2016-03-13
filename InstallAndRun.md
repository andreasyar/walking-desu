# Introduction #

This page helps you get, build and run **Walking Desu** (**Wandering** further) server and client(s).

# Linux #

1. Create a directory for working copy of **Wandering**

```
[sorc@mars ~]$ cd src
[sorc@mars src]$ mkdir wandering
[sorc@mars src]$ cd wandering/
[sorc@mars wandering]$ pwd
/home/sorc/src/wandering
[sorc@mars wandering]$
```

2. Now you inside a new directory. Create a working copy within

```
[sorc@mars wandering]$ svn checkout http://walking-desu.googlecode.com/svn/trunk/ ./
...
Checked out revision 81.
[sorc@mars wandering]$ ls -l
total 20
-rw-rw-r--. 1 sorc sorc 3654 2010-08-12 12:53 build-before-profiler.xml
-rw-rw-r--. 1 sorc sorc  842 2010-08-12 12:53 build.xml
-rw-rw-r--. 1 sorc sorc   82 2010-08-12 12:53 manifest.mf
drwxrwxr-x. 4 sorc sorc 4096 2010-08-12 12:53 nbproject
drwxrwxr-x. 5 sorc sorc 4096 2010-08-12 12:53 src
[sorc@mars wandering]$
```

3. Download sprites archive img.tar.bz2 and extract it into **src** subdirectory of **wandering** directory

```
[sorc@mars wandering]$ cd src
[sorc@mars src]$ download my sprites!
bash: download: command not found
[sorc@mars src]$ ls -l img*
-rw-rw-r--. 1 sorc sorc 346665 2010-08-12 13:03 img.tar.bz2
[sorc@mars src]$ tar -xvjf img.tar.bz2
...
[sorc@mars src]$ ls -l img*
-rw-rw-r--.  1 sorc sorc 346665 2010-08-12 13:03 img.tar.bz2

img:
total ...
...
[sorc@mars src]$
```

4. Make all shell scripts inside executable

```
[sorc@mars src]$ ls -l *.sh
-rw-rw-r--. 1 sorc sorc  75 2010-08-12 12:53 mkclientjar.sh
-rwxrwxr-x. 1 sorc sorc  45 2010-08-12 12:53 wdscomp.sh
-rwxrwxr-x. 1 sorc sorc 437 2010-08-12 12:53 wds-java.sh
-rwxrwxr-x. 1 sorc sorc 313 2010-08-12 12:53 wds-scheme.sh
[sorc@mars src]$ chmod ug+x *.sh
[sorc@mars src]$ ls -l *.sh
-rwxrwxr--. 1 sorc sorc  75 2010-08-12 12:53 mkclientjar.sh
-rwxrwxr-x. 1 sorc sorc  45 2010-08-12 12:53 wdscomp.sh
-rwxrwxr-x. 1 sorc sorc 437 2010-08-12 12:53 wds-java.sh
-rwxrwxr-x. 1 sorc sorc 313 2010-08-12 12:53 wds-scheme.sh
[sorc@mars src]$
```

5. Compile server and create a client **.jar** archive

```
[sorc@mars src]$ ./wdscomp.sh 
[sorc@mars src]$ ./mkclientjar.sh 
...                                                              ^
... warnings
[sorc@mars src]$
```

6. On another console start a server. Logging enabled by default

```
[sorc@mars ~]$ cd src/wandering/src/
[sorc@mars src]$ ./wds-java.sh 
Logs directory not exist. Lets create ...
Starting Java Test Server.
Server starts at port: 45000
...
```

7. Start Wandering client and play!

```
[sorc@mars src]$ ls -l *.jar
-rw-rw-r--. 1 sorc sorc 531628 2010-08-12 13:13 WD.jar
[sorc@mars src]$ java -jar WD.jar localhost 45000
--> (hello)
--> (nick "Desu")
<-- (hello 86 100 0.07 0 0 351916 "Desu" "SOUTH" "peasant")
...
```

8. If you find some bugs, receive a exceptions or another bad things - please report about it. You can create a **Issues** here.

9. Controls:

  * Esc - unselect unit
  * hold T - show tower range and movement trace
  * Shift + mouse left click - select unit
  * F3 - attack selected unit
  * F4 - build tower

# Windows #

Use **Power Shell** for all actions described below.

1. Create a directory for working copy of **Wandering**

```
PS C:\Documents and Settings\CatsPaw> mkdir src\wandering


    Каталог: C:\Documents and Settings\CatsPaw\src


Mode                LastWriteTime     Length Name
----                -------------     ------ ----
d----        12.08.2010     20:39            wandering


PS C:\Documents and Settings\CatsPaw> cd src\wandering
PS C:\Documents and Settings\CatsPaw\src\wandering> pwd

Path
----
C:\Documents and Settings\CatsPaw\src\wandering


PS C:\Documents and Settings\CatsPaw\src\wandering>
```

2. Now you inside a new directory. Create a working copy within

```
PS C:\Documents and Settings\CatsPaw\src\wandering> svn checkout http://walking-desu.googlecode.com/svn/trunk/ ./
...
Checked out revision 83.
PS C:\Documents and Settings\CatsPaw\src\wandering> ls


    Каталог: C:\Documents and Settings\CatsPaw\src\wandering


Mode                LastWriteTime     Length Name
----                -------------     ------ ----
d----        12.08.2010     20:48            nbproject
d----        12.08.2010     20:48            src
-a---        12.08.2010     20:48       3654 build-before-profiler.xml
-a---        12.08.2010     20:48        842 build.xml
-a---        12.08.2010     20:48         82 manifest.mf


PS C:\Documents and Settings\CatsPaw\src\wandering>
```

3. Download sprites archive img.tar.bz2 and extract it into **src** subdirectory of **wandering** directory. You can use any another archivator program of course.

```
PS C:\Documents and Settings\CatsPaw\src\wandering> cd src
PS C:\Documents and Settings\CatsPaw\src\wandering\src> download my sprites!
Имя "download" не распознано как имя командлета, функции, файла скрипта или выполняемой программы.
Проверьте правильность написания имени, а также наличие и правильность пути, после чего повторите попытку.
строка:1 знак:9
+ download <<<<  my sprites!
    + CategoryInfo          : ObjectNotFound: (download:String) [], CommandNotFoundException
    + FullyQualifiedErrorId : CommandNotFoundException

PS C:\Documents and Settings\CatsPaw\src\wandering\src> ls img*


    Каталог: C:\Documents and Settings\CatsPaw\src\wandering\src


Mode                LastWriteTime     Length Name
----                -------------     ------ ----
-a---        12.08.2010     20:58     346665 img.tar.bz2


PS C:\Documents and Settings\CatsPaw\src\wandering\src>
PS C:\Documents and Settings\CatsPaw\src\wandering> $env:Path = $env:Path + ";C:\Program Files\7-Zip"
PS C:\Documents and Settings\CatsPaw\src\wandering\src> `7z.exe x .\img.tar.bz2
...
PS C:\Documents and Settings\CatsPaw\src\wandering\src> `7z.exe x .\img.tar
...
PS C:\Documents and Settings\CatsPaw\src\wandering\src> ls img*


    Каталог: C:\Documents and Settings\CatsPaw\src\wandering\src


Mode                LastWriteTime     Length Name
----                -------------     ------ ----
d----        12.08.2010     21:08            img
-a---        12.08.2010     20:58     829440 img.tar
-a---        12.08.2010     20:58     346665 img.tar.bz2


PS C:\Documents and Settings\CatsPaw\src\wandering\src>
```

4. Change your execution policy in power shell. If you have no access to administrator account on your PC and execution policy is not **RemoteSigned** you have a big problem. Go find your administrator and say him what you want access to create commandlets.

```
PS C:\Documents and Settings\CatsPaw\src\wandering\src> Get-ExecutionPolicy
RemoteSigned
PS C:\Documents and Settings\CatsPaw\src\wandering\src> "This policy is ok"
This policy is ok
```

5. Compile server and create a client **.jar** archive

```
PS C:\Documents and Settings\CatsPaw\src\wandering\src> .\wdscomp.ps1
PS C:\Documents and Settings\CatsPaw\src\wandering\src> .\mkclientjar.ps1
...                                                                         ^
... warnings
PS C:\Documents and Settings\CatsPaw\src\wandering\src>
```

6. On another console start a server. Logging enabled by default

```
PS C:\Documents and Settings\CatsPaw> cd .\src\wandering\src
PS C:\Documents and Settings\CatsPaw\src\wandering\src> .\wds-java.ps1
Logs directory not exist. Lets create ...


    Каталог: C:\Documents and Settings\CatsPaw\src\wandering\src\server\javatestserver


Mode                LastWriteTime     Length Name
----                -------------     ------ ----
d----        12.08.2010     21:22            logs
Starting Java Test Server.
Server starts at port: 45000
...
```

7. Start Wandering client and play!

```
PS C:\Documents and Settings\CatsPaw\src\wandering\src> ls *.jar


    Каталог: C:\Documents and Settings\CatsPaw\src\wandering\src


Mode                LastWriteTime     Length Name
----                -------------     ------ ----
-a---        12.08.2010     21:21     531628 WD.jar


PS C:\Documents and Settings\CatsPaw\src\wandering\src> java -jar .\WD.jar localhost 45000
--> (hello)
--> (nick "Desu")
<-- (hello 91 100 0.07 0 0 363546 "Desu" "SOUTH" "peasant")
...
```

8. If you find some bugs, receive a exceptions or another bad things - please report about it. You can create a **Issues** here.

9. Controls:

  * Esc - unselect unit
  * hold T - show tower range and movement trace
  * Shift + mouse left click - select unit
  * F3 - attack selected unit
  * F4 - build tower