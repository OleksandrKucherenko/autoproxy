# GPG - Signing Key

Used for signing binaries. Without passphrase it is useless.

# Import Key

```bash
gpg --import gradle/private_key_sender.asc
```

Output:

```
gpg: key A28F2B36: secret key imported
gpg: key A28F2B36: "Oleksandr Kucherenko (JCentral Publishing Key) <kucherenko.alex@gmail.com>" not changed
gpg: Total number processed: 1
gpg:              unchanged: 1
gpg:       secret keys read: 1
gpg:   secret keys imported: 1
```

# Verify Key

```bash
gpg --list-keys
```

Output:
```
/c/Users/alexk/.gnupg/pubring.gpg
---------------------------------
pub   2048R/A28F2B36 2017-12-22
uid                  Oleksandr Kucherenko (JCentral Publishing Key) <kucherenko.alex@gmail.com>
sub   2048R/CC429755 2017-12-22

```