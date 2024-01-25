# File Encoder
This Minecraft plugin allows you to encode and decode any files into your minecraft world without loss of quality.
It serves as an alternative secret file storage and teaches principles of how computers store and interpret data.

# How does it work
Computers are all about numbers. Computers dont use our Decimal System (Base 10) but rather the Binary System (Base 2) which is implemented using Bits. A Bit is the most basic unit and represents two states: On (=1) and Off (=0).
A Byte is a collection of 8 Bits and therefore can hold 2^8 = 256 possible values.
Every file is a collection of Bytes (Collection of 8 Bits) and Bits which are interpreted as whatever the file is supposed to be. E.g. in a textfile the letters are encoded as bytes that correspond to the ascii letter,
in an image file the bytes encode the rgb values for the pixels, and so on...

This plugin reads the bytes from the file you give it and encoded it into minecraft blocks and back. It does this using a Lookup Table of Minecraft Blocks that correspond to individual Bits and Nibbles (4 Bits).
To be as efficient as possible the plugin utilizes a two way Hashmap and Buffering for reading and writing from/to files to limit IO as much as possible.

The chose of minecraft blocks has to be considered very carefully as the blocks have to fulfill the following criteria to allow for decoding without loss of quality or file corruption:
The block...

1) ... musn't be affected by gravity (e.g. Anvil)
2) ... musn't affect or destroy other blocks in any way (e.g. Lava, Piston, TNT, Lava)
3) ... musn't change due to time or weather conditions (e.g. copper which oxidizes over time -> This is the reason this plugin uses waxed copper blocks)
4) ... must be able to be placed anywhere (e.g. not torches)

This reduced the number of blocks to use immensely. Originally i intended to encode the Blocks in Base 256 which would allow to encode a whole byte into just one Block. This would reduce the file size by two in comparison to hexadecimal encoding and by 8 in comparison to binary
encoding. The reason i didnt do it was i wasnt able to find 256 Blocks meeting this criteria (Also im sure it would be possible because i excluded a lot of blocks i didnt know but that in hindsight would definitely fullfill the criteria). Feel free to add Base 256 Encoding to this plugin :)

# Encoding of files
The plugin supports two modes of Encoding: Base 2(Binary) and Base 16 (Hexadecimal) which are equivalent but vary in "file size". The Base 2 encryption mode encodes the each bit of the file with either a white block representing a 0 and a black block representing a 1.
It therefore uses 8 blocks for a Byte because 2^8 = 256.
The Base 16 mode does the same thing but using 16 Blocks for the numbers 123456789ABCDEF. Because we need to encode a Byte we need to be able to encode 256 values which is 16^2. This means we can encode any Byte with two Blocks in the Base 16 System and therefore we only need a fourth of the number of blocks.

# Decoding of files
Decoding of a file works in the exact same way but reverse. We build the Byte from the minecraft blocks and then write it into the file.
To know the file extension and encoding mode it uses a so called encoding marking which consists of a special block (representing the highest number of the number system = Base - 1) and a sign which denotes the filename and the number of bytes encoded in hexadecimal on the back.

# How to use
Place the plugin into your servers plugins folder and restart. The plugin will automatically create a folder called FileEncoder in your plugins folder. Now place your file into the Folder and in Minecraft isssue the command `/encode`. This will encode the file using Base 16.
To encode the file using Base 2 type `/encode base2`. Depending on the filesize this might take a while and the server will become unresponsive (depending on the server timeout it might lead to a automated server restart. In that case increase the time out interval).

If you want to decode the file again stand on the encoding marking and just type `decode`. This will delete the blocks and create the file in the same folder again.

Tip: Its very interesting to see what happens when u break some blocks :)

# Screenshots
