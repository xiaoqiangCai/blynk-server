package cc.blynk.server.core.model.widgets.ui.table;

import cc.blynk.server.core.model.enums.PinMode;
import cc.blynk.server.core.model.enums.PinType;
import cc.blynk.server.core.model.storage.MultiPinStorageValue;
import cc.blynk.server.core.model.storage.MultiPinStorageValueType;
import cc.blynk.server.core.model.storage.PinStorageValue;
import cc.blynk.server.core.model.widgets.OnePinWidget;
import cc.blynk.utils.structure.TableLimitedQueue;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.utils.StringUtils.BODY_SEPARATOR_STRING;


/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 28.03.16.
 */
public class Table extends OnePinWidget {

    public Column[] columns;

    public final TableLimitedQueue<Row> rows = new TableLimitedQueue<>();

    public volatile int currentRowIndex;

    public boolean isReoderingAllowed;

    public boolean isClickableRows;

    @Override
    public void sendHardSync(ChannelHandlerContext ctx, int msgId, int deviceId) {
    }

    @Override
    public boolean updateIfSame(int deviceId, byte pin, PinType type, String value) {
        if (isSame(deviceId, pin, type)) {
            var values = value.split(BODY_SEPARATOR_STRING);
            if (values.length > 0) {
                String tableCommand = values[0];
                switch (tableCommand) {
                    case "clr" :
                        rows.clear();
                        currentRowIndex = 0;
                        break;
                    case "add" :
                        if (values.length > 3) {
                            int id = Integer.parseInt(values[1]);
                            String rowName = values[2];
                            String rowValue = values[3];
                            Row existingRow = get(id);
                            if (existingRow == null) {
                                rows.add(new Row(id, rowName, rowValue, true));
                            } else {
                                existingRow.update(rowName, rowValue);
                            }
                        }
                        break;
                    case "update" :
                        if (values.length > 3) {
                            int id = Integer.parseInt(values[1]);
                            String rowName = values[2];
                            String rowValue = values[3];
                            Row existingRow = get(id);
                            if (existingRow != null) {
                                existingRow.update(rowName, rowValue);
                            }
                        }
                        break;
                    case "pick" :
                        if (values.length > 1) {
                            currentRowIndex = Integer.parseInt(values[1]);
                        }
                        break;
                    case "select" :
                        if (values.length > 1) {
                            selectRow(values[1], true);
                        }
                        break;
                    case "deselect" :
                        if (values.length > 1) {
                            selectRow(values[1], false);
                        }
                        break;
                }
                this.value = value;
            }
            return true;
        }
        return false;
    }

    private void selectRow(String idString, boolean select) {
        int id = Integer.parseInt(idString);
        Row row = get(id);
        if (row != null) {
            row.isSelected = select;
        }
    }

    public Row get(int id) {
        for (Row row : rows) {
            if (id == row.id) {
                return row;
            }
        }
        return null;
    }

    @Override
    public PinStorageValue getPinStorageValue() {
        return new MultiPinStorageValue(MultiPinStorageValueType.TABLE);
    }

    @Override
    public PinMode getModeType() {
        return PinMode.out;
    }

    @Override
    public int getPrice() {
        return 800;
    }

    @Override
    public void erase() {
        super.erase();
        rows.clear();
    }
}
