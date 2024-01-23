package spigot.zanos.plugin.commands;

import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import spigot.zanos.plugin.FileEncoder;
import spigot.zanos.plugin.enums.EncodingMode;
import spigot.zanos.plugin.lookuptable.LUT;

import java.io.*;

public class CommandDecode implements CommandExecutor {

    public static final String name = "decode";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may issue this command!");
            return true;
        }

        Player player = (Player) sender;
        Location playerLocation = player.getLocation();
        File filesDir = FileEncoder.getPlugin(FileEncoder.class).getDataFolder();
        File[] files = filesDir.listFiles();

        if (!filesDir.exists() || !filesDir.isDirectory() || files == null) {
            sender.sendMessage(ChatColor.RED + "Plugin directory not found! (Try restarting the server)");
            return true;
        }

        if (files.length != 0) {
            sender.sendMessage(ChatColor.RED + "Plugin directory not empty!");
            return true;
        }

        // check if player is standing on an encoding sign
        Material m = playerLocation.getBlock().getType();
        if (!(m == Material.BIRCH_SIGN)) {
            sender.sendMessage(ChatColor.RED + "You have to stand on an Encoding marking to issue this command!");
            return true;
        }
        Sign sign = (Sign) playerLocation.getBlock().getState();
        /*
        if(!sign.isWaxed()){
            sender.sendMessage(ChatColor.RED + "You have to stand on an Encoding marking to issue this command!");
            return true;
        }*/

        String[] lines = CommandEncode.readFromSign(playerLocation, Side.FRONT);
        StringBuilder str = new StringBuilder(60);
        for (String s : lines) {
            str.append(s);
        }

        String filename = str.toString();

        if (filename.trim().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Invalid file name!");
            return true;
        }

        // read back of sign to get the number of bytes to read in total
        String[] linesBack = CommandEncode.readFromSign(playerLocation, Side.BACK);
        StringBuilder str2 = new StringBuilder(60);
        for (String s : linesBack) {
            str2.append(s);
        }

        if (str2.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "The sign father says there is no bytes to read!");
            return true;
        }

        long numberOfBytesToDecode = Long.parseLong(str2.toString(), 16);

        if (numberOfBytesToDecode <= 0) {
            sender.sendMessage(ChatColor.RED + "The sign father says there is no bytes to read!");
            return true;
        }

        // identify encoding mode

        Location encodingBlockLocation = new Location(playerLocation.getWorld(), playerLocation.getBlockX(), playerLocation.getBlockY() - 1, playerLocation.getBlockZ());
        Material encodingBlockMaterial = encodingBlockLocation.getBlock().getType();

        EncodingMode encodingMode;
        if (LUT.MATERIAL_TO_INDEX.get(encodingBlockMaterial) == 1) {
            // base2 encoding
            sender.sendMessage("Detected Base2 Encoding");
            encodingMode = EncodingMode.Base2;
        } else if (LUT.MATERIAL_TO_INDEX.get(encodingBlockMaterial) == 15) {
            // base 16 encoding
            encodingMode = EncodingMode.BASE16;
            sender.sendMessage("Detected Base16 Encoding");
        } else {
            // invalid
            sender.sendMessage(ChatColor.RED + "Unrecognized Encoding");
            return true;
        }

        // create file

        File file = new File(filesDir.getAbsolutePath() + File.separator + filename);

        try {
            file.createNewFile();
            sender.sendMessage("Created file: " + file.getAbsolutePath());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Couldn't create file " + "\"" + filename + "\". Reason:" + e.getMessage());
            return true;
        }


        int bytesDecoded = 0;
        if (encodingMode == EncodingMode.BASE16) {
            // base16 encoding

            // iterate through the blocks and write back the bytes into the file

            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true))) {

                outerloop:
                for (int depthIndex = 0; ; ++depthIndex) {
                    for (int rowIndex = 0; rowIndex < FileEncoder.MAX_COLUMN_HEIGHT; ++rowIndex) {
                        for (int columnIndex = 0; columnIndex < FileEncoder.MAX_ROW_WIDTH; columnIndex += 2) {

                            if (bytesDecoded >= numberOfBytesToDecode) {
                                break outerloop;
                            }

                            // two blocks/nibbles make up one byte
                            int x = playerLocation.getBlockX() - FileEncoder.X_OFFSET - depthIndex;
                            int y = playerLocation.getBlockY() + rowIndex;
                            int z1 = playerLocation.getBlockZ() + columnIndex;
                            int z2 = playerLocation.getBlockZ() + columnIndex + 1;

                            Location upperNibbleLocation = new Location(playerLocation.getWorld(), x, y, z1);
                            Location lowerNibbleLocation = new Location(playerLocation.getWorld(), x, y, z2);

                            Material upperNibbleMaterial = upperNibbleLocation.getBlock().getType();
                            Material lowerNibbleMaterial = lowerNibbleLocation.getBlock().getType();

                            int upperNibble;
                            int lowerNibble;
                            int byteToWrite = 0x0;

                            // check if there is still blocks to decode
                            // if we encounter unknown blocks we write null bytes
                            if (LUT.materialSet.contains(upperNibbleMaterial) && LUT.materialSet.contains(lowerNibbleMaterial)) {
                                upperNibble = LUT.MATERIAL_TO_INDEX.get(upperNibbleMaterial);
                                lowerNibble = LUT.MATERIAL_TO_INDEX.get(lowerNibbleMaterial);

                                byteToWrite = (upperNibble & 0xF) << 4;
                                byteToWrite |= lowerNibble & 0xF;
                            }

                            // write byte to file
                            bos.write(byteToWrite);

                            // delete blocks
                            upperNibbleLocation.getBlock().setType(Material.AIR);
                            lowerNibbleLocation.getBlock().setType(Material.AIR);

                            bytesDecoded++;
                        }
                    }
                }

            } catch (FileNotFoundException fnfe) {
                sender.sendMessage(ChatColor.RED + "Output file not found!");
                return true;
            } catch (IOException ioe) {
                sender.sendMessage(ChatColor.RED + "Error writing file: " + ioe.getMessage());
                return true;
            }

        } else {
            // base2 encoding
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true))) {

                outerloop:
                for (int depthIndex = 0; ; ++depthIndex) {
                    for (int rowIndex = 0; rowIndex < FileEncoder.MAX_COLUMN_HEIGHT; ++rowIndex) {
                        for (int columnIndex = 0; columnIndex < FileEncoder.MAX_ROW_WIDTH; columnIndex += 8) {

                            if (bytesDecoded >= numberOfBytesToDecode) {
                                break outerloop;
                            }

                            int byteToWrite = 0;
                            // 8 blocks/bits make up one byte
                            for(int i = 0; i < 8;++i){
                                // get the block
                                int x = playerLocation.getBlockX() - FileEncoder.X_OFFSET - depthIndex;
                                int y = playerLocation.getBlockY() + rowIndex;
                                int z = playerLocation.getBlockZ() + columnIndex + i;

                                Location bitBlockLocation = new Location(playerLocation.getWorld(),x,y,z);
                                Material bitBlockMaterial = bitBlockLocation.getBlock().getType();

                                int bit = 0;

                                if(LUT.materialSet.contains(bitBlockMaterial)){
                                    bit = LUT.MATERIAL_TO_INDEX.get(bitBlockMaterial);
                                    if(bit > 1){
                                        sender.sendMessage(ChatColor.RED + "Wrong encoding!");
                                        return true;
                                    }

                                    byteToWrite |= (bit << i);

                                    // delete block
                                    bitBlockLocation.getBlock().setType(Material.AIR);
                                }
                            }

                            // write byte
                            bos.write(byteToWrite);

                            bytesDecoded++;
                        }
                    }
                }
            } catch (FileNotFoundException fnfe) {
                sender.sendMessage(ChatColor.RED + "Output file not found!");
                return true;
            } catch (IOException ioe) {
                sender.sendMessage(ChatColor.RED + "Error writing file: " + ioe.getMessage());
                return true;
            }
        }

        // delete sign and encoding block
        playerLocation.getBlock().setType(Material.AIR);
        encodingBlockLocation.getBlock().setType(Material.GRASS_BLOCK);

        sender.sendMessage(ChatColor.GREEN + "SUCCESS: Decoded " + bytesDecoded + " Bytes into file: " + "\"" + filename + "\"");

        return true;


    }
}
