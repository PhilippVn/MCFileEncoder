package spigot.zanos.plugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import spigot.zanos.plugin.FileEncoder;
import spigot.zanos.plugin.enums.EncodingMode;
import spigot.zanos.plugin.lookuptable.LUT;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommandEncode implements CommandExecutor {

    public static final String name = "encode";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may issue this command!");
            return true;
        }

        if (args.length > 1) {
            return false;
        }

        Player player = (Player) sender;

        File filesDir = FileEncoder.getPlugin(FileEncoder.class).getDataFolder();
        File[] files = filesDir.listFiles();

        if (!filesDir.exists() || !filesDir.isDirectory() || files == null) {
            sender.sendMessage(ChatColor.RED + "Plugin directory not found! (Try restarting the server)");
            return true;
        }

        if (files.length == 0) {
            sender.sendMessage(ChatColor.RED + "No file to encode! Be sure to place your file in " + filesDir.toPath().toAbsolutePath());
            return true;
        }

        if (files.length > 1) {
            sender.sendMessage(ChatColor.RED + "U may only encode one file at a time!");
            return true;
        }

        File file = files[0];
        if (!file.isFile()) {
            sender.sendMessage(ChatColor.RED + "\"" + file.getName() + "\"" + " is not a File!");
            return true;
        }

        EncodingMode mode;

        if (args.length == 0) {
            // default: bytemode
            mode = EncodingMode.BASE16;
        } else {
            if (args[0].equalsIgnoreCase("base2")) {
                // bitmode
                mode = EncodingMode.Base2;
            } else if (args[0].equalsIgnoreCase("base16")) {
                mode = EncodingMode.BASE16;
            } else {
                return false;
            }
        }

        sender.sendMessage("Encoding file " + "\"" + file.getName() + "\"" + "...");

        Location playerLocation = player.getLocation();

        String[] signText = stringToSignText(file.getName());
        if (signText == null) {
            sender.sendMessage(ChatColor.RED + "Filename: \"" + file.getName() + "\"" + " is too long (max 60 characters allowed)");
            return true;
        }
        createSign(playerLocation, signText);

        long bytesEncoded = 0;
        if (mode == EncodingMode.BASE16) {

            // for decoding: make the block under the sign the block with the highest material number possible in the base n (n-1)
            markEncoding(16,new Location(playerLocation.getWorld(),playerLocation.getBlockX(),playerLocation.getBlockY() - 1,playerLocation.getBlockZ()));


            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {

                outerLoop:
                for (int depthIndex = 0; ; ++depthIndex) {
                    for (int rowIndex = 0; rowIndex < FileEncoder.MAX_COLUMN_HEIGHT; ++rowIndex) {
                        for (int columnIndex = 0; columnIndex < FileEncoder.MAX_ROW_WIDTH; columnIndex += 2) {
                            int byteRead = inputStream.read();
                            if (byteRead == -1) {
                                break outerLoop;
                            }

                            int upperNibble = (byteRead & 0xF0) >> 4; // Extract upper 4 bits
                            int lowerNibble = byteRead & 0x0F;         // Extract lower 4 bits

                            Material upperNibbleMaterial = LUT.INDEX_TO_MATERIAL.get(upperNibble);
                            Material lowerNibbleMaterial = LUT.INDEX_TO_MATERIAL.get(lowerNibble);

                            int x = playerLocation.getBlockX() - FileEncoder.X_OFFSET - depthIndex;
                            int y = playerLocation.getBlockY() + rowIndex;
                            int z1 = playerLocation.getBlockZ() + columnIndex;
                            int z2 = playerLocation.getBlockZ() + columnIndex + 1;

                            Location location1 = new Location(playerLocation.getWorld(), x, y, z1);
                            Location location2 = new Location(playerLocation.getWorld(), x, y, z2);

                            location1.getBlock().setType(upperNibbleMaterial);
                            location2.getBlock().setType(lowerNibbleMaterial);
                            bytesEncoded++;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                sender.sendMessage(ChatColor.RED + "File: \"" + file.getName() + "\"" + " not found");
                return true;
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Encoding Exception: " + e.getMessage());
                return true;
            }



        } else {
            // bitmode

            markEncoding(2,new Location(playerLocation.getWorld(),playerLocation.getBlockX(),playerLocation.getBlockY() - 1,playerLocation.getBlockZ()));

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {

                outerLoop:
                for (int depthIndex = 0; ; ++depthIndex) {
                    for (int rowIndex = 0; rowIndex < FileEncoder.MAX_COLUMN_HEIGHT; ++rowIndex) {
                        for (int columnIndex = 0; columnIndex < FileEncoder.MAX_ROW_WIDTH; columnIndex += 8) {

                            int byteRead = inputStream.read();
                            if (byteRead == -1) {
                                break outerLoop;
                            }

                            // iterate through each bit of byteRead
                            for(int i = 0; i < 8; ++i){
                                Material material = LUT.INDEX_TO_MATERIAL.get((byteRead >> i) & 1);
                                int x = playerLocation.getBlockX() - FileEncoder.X_OFFSET - depthIndex;
                                int y = playerLocation.getBlockY() + rowIndex;
                                int z = playerLocation.getBlockZ() + columnIndex + i;

                                Location location = new Location(playerLocation.getWorld(),x,y,z);
                                location.getBlock().setType(material);
                            }
                            bytesEncoded++;
                        }
                    }
                }


            } catch (FileNotFoundException e) {
                sender.sendMessage(ChatColor.RED + "File: \"" + file.getName() + "\"" + " not found");
                return true;
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + "Encoding Exception: " + e.getMessage());
                return true;
            }
        }

        // write number of bytes encoded on back of the sign (used for decoding)
        writeOnSign(playerLocation,Side.BACK,stringToSignText(Long.toHexString(bytesEncoded)));

        sender.sendMessage(ChatColor.GREEN + "SUCCESS: Encoded " + bytesEncoded + " Bytes!");

        // delete file
        try {
            file.delete();
        } catch(Exception ignored){}

        return true;
    }

    public static void createSign(Location location, String[] lines) {
        location.getBlock().setType(Material.BIRCH_SIGN);
        writeOnSign(location,Side.FRONT,lines);
    }

    public static void writeOnSign(Location signLocation, Side side,String[] lines){
        Material m = signLocation.getBlock().getType();
        if(m != Material.BIRCH_SIGN){
            return;
        }

        Sign s = (Sign) signLocation.getBlock().getState();

        SignSide ss =  s.getSide(side);
        for (int i = 0; i < Math.min(lines.length, 4); i++) {
            ss.setLine(i, lines[i]);
        }
        s.setWaxed(true);
        s.update();
    }

    public static String[] readFromSign(Location signLocation, Side side){
        Material m = signLocation.getBlock().getType();
        if(m != Material.BIRCH_SIGN){
            return null;
        }

        Sign s = (Sign) signLocation.getBlock().getState();
        SignSide ss =  s.getSide(side);

        return ss.getLines();
    }

    public static String[] stringToSignText(String str) {
        // a sign has 15 columns and 4 rows
        if (str.length() > 60) {
            return null;
        }

        List<String> result = new ArrayList<>();

        for (int i = 0; i < str.length(); i += 15) {
            // Calculate the end index for the substring
            int endIndex = Math.min(i + 15, str.length());

            // Extract the substring and add it to the result list
            String substring = str.substring(i, endIndex);
            result.add(substring);
        }

        // Convert the list to an array
        return result.toArray(new String[0]);

    }

    public static void markEncoding(int base, Location location){
        Material material = LUT.INDEX_TO_MATERIAL.get(base-1);
        location.getBlock().setType(material);
    }
}
