package es.lifevit.sdk.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

////////////////////// Helper Classes: BleUtil and BleAdvertisedData ///////////////
public class BLEUtil {
    private final static String TAG=BLEUtil.class.getSimpleName();
    public static BLEAdvertisedData parseAdertisedData(byte[] advertisedData) {
        try {
            List<UUID> uuids = new ArrayList<>();
            String name = null;
            if (advertisedData == null) {
                return new BLEAdvertisedData(uuids, name);
            }

            ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
            while (buffer.remaining() > 2) {
                byte length = buffer.get();
                if (length == 0) break;

                byte type = buffer.get();
                switch (type) {
                    case 0x02: // Partial list of 16-bit UUIDs
                    case 0x03: // Complete list of 16-bit UUIDs
                        while (length >= 2) {
                            uuids.add(UUID.fromString(String.format(
                                    "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                            length -= 2;
                        }
                        break;
                    case 0x06: // Partial list of 128-bit UUIDs
                    case 0x07: // Complete list of 128-bit UUIDs
                        while (length >= 16) {
                            long lsb = buffer.getLong();
                            long msb = buffer.getLong();
                            uuids.add(new UUID(msb, lsb));
                            length -= 16;
                        }
                        break;
                    case 0x09:
                        byte[] nameBytes = new byte[length - 1];
                        buffer.get(nameBytes);
                        name = new String(nameBytes, StandardCharsets.UTF_8);
                        break;
                    default:
                        buffer.position(buffer.position() + length - 1);
                        break;
                }
            }
            return new BLEAdvertisedData(uuids, name);
        }
        catch (Exception e){

        }
        return null;
    }
}