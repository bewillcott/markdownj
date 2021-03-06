/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.markdownj;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

/**
 * Holds all the information parsed from, and about, a table row.
 *
 * @author Bradley Willcott
 */
class TableRow {

    private static final int DEFAULT_BORDERWIDTH = 1;
    private static final int DEFAULT_CELLPADDING = 5;

    /**
     * Parses the row text, breaking out and storing all its components.
     *
     * @param text The text of the row to parse and store.
     *
     * @return A fully configured new instance of this class.
     */
    public static TableRow parse(final String text) {
        TableRow tr = new TableRow(text);
        String data = text.substring(1).trim();
        tr.cells = data.split("\\|");
        tr.length = tr.cells.length;
        String attrib = tr.cells[tr.length - 1];

        if (attrib.startsWith("[") && attrib.endsWith("]")) {
            if (attrib.length() == 2) {
                tr.status.setBorder();
            } else {
                tr.classes = attrib;
                tr.status.setClasses();
            }
        }

        if (tr.status.hasAttribute()) {
            tr.length -= 1;

            if (tr.status.hasClasses()) {
                if (tr.classes.substring(1, attrib.length() - 1).trim().matches("^\\d+$")) {
                    tr.borderWidth = parseInt(tr.classes.substring(1, attrib.length() - 1).trim());
                    tr.status.setBorder();
                } else {
                    Matcher m = Pattern.compile("^(?:\\[#(?<id>\\w+)\\])?$").matcher(tr.classes);

                    if (m.find()) {
                        tr.id = m.group("id");
                        tr.status.setId();
                        tr.status.unsetClasses();

                    } else {
                        Matcher m2 = Pattern.compile("^(?:\\[#(?<id>\\w*)?\\])?"
                                                     + "\\[(?<border>(?<borderWidth>\\d+)"
                                                     + "(?:(?:[, ][ ]*)(?<cellPadding>\\d+))?)?\\]$")
                                .matcher(tr.classes);

                        if (m2.find()) {
                            tr.id = m2.group("id");

                            if (tr.id != null && !tr.id.isBlank()) {
                                tr.status.setId();
                            }

                            String border = m2.group("border");

                            if (border == null) {
                                tr.status.setBorder();
                            } else {
                                tr.borderWidth = parseInt(m2.group("borderWidth"));
                                String cellPadding = m2.group("cellPadding");

                                if (cellPadding != null) {
                                    tr.cellPadding = parseInt(cellPadding);
                                }

                                tr.status.setBorder();
                            }
                        } else {
                            Matcher m3 = Pattern.compile("^(?:\\[#(?<id>\\w*)?\\])?\\[(?<classes>[^\\]]+)\\]$").matcher(tr.classes);

                            if (m3.find()) {
                                tr.id = m3.group("id");

                                if (tr.id != null && !tr.id.isBlank()) {
                                    tr.status.setId();
                                }

                                tr.classes = m3.group("classes");
                            } else {
                                tr.status.unsetClasses();
                            }
                        }
                    }
                }
            }
        }

        return tr;
    }

    public final String text;
    private int borderWidth = -1;
    private int cellPadding = -1;
    private String[] cells;
    private String classes = null;
    private String id = "";
    private int length = 0;
    private boolean readOnly = false;
    private final Status status = new Status();

    // Restrict instaniation to the static method: parse()
    private TableRow(String text) {
        this.text = text;
    }

    /**
     *
     * @return Either the valued parsed from the row text, or the default value.
     */
    public int getBorderWidth() {
        return hasBorderWidth() ? borderWidth : DEFAULT_BORDERWIDTH;
    }

    /**
     *
     * @param index Must be: {@code 0 <= index < length}
     *
     * @return Contents of the indexed cell.
     */
    public String getCell(int index) {
        if (index >= 0 && index < length) {
            return cells[index];
        } else {
            throw new IndexOutOfBoundsException(index);
        }
    }

    /**
     *
     * @return Either the valued parsed from the row text, or the default value.
     */
    public int getCellPadding() {
        return hasCellPadding() ? cellPadding : DEFAULT_CELLPADDING;
    }

    /**
     * Returns the text of the classes parsed from the row text.
     * <p>
     * You might want to test {@link #hasClasses()} before calling this method.
     * </p>
     *
     * @return Either the classes text, or {@code null} if not set.
     */
    public String getClasses() {
        return hasClasses() ? classes : null;
    }

    /**
     * Returns the text of the id parsed from the row text.
     * <p>
     * You might want to test {@link #has} before calling this method.
     * </p>
     *
     * @return Either the classes text, or {@code null} if not set.
     */
    public String getId() {
        return hasId() ? id : null;
    }

    /**
     *
     * @return {@code True} if attributes stored.
     */
    public boolean hasAttrib() {
        return status.hasAttribute();
    }

    /**
     *
     * @return {@code True} if the border attribute parsed from row text.
     */
    public boolean hasBorder() {
        return status.hasBorder();
    }

    /**
     *
     * @return {@code True} if borderWidth attribute parsed from row text.
     */
    public boolean hasBorderWidth() {
        return hasBorder() && borderWidth > -1;
    }

    /**
     *
     * @return {@code True} if cellPadding attribute parsed from row text.
     */
    public boolean hasCellPadding() {
        return hasBorder() && cellPadding > -1;
    }

    /**
     *
     * @return {@code True} if one or more class attributes were parsed from row text.
     */
    public boolean hasClasses() {
        return status.hasClasses();
    }

    /**
     *
     * @return {@code True} if id was parsed from row text.
     */
    public boolean hasId() {
        return status.hasId();
    }

    /**
     * This is the number of columns within this row.
     *
     * @return The number of columns.
     */
    public int length() {
        return length;
    }

    public boolean setCell(int index, String text) {
        boolean rtn = false;

        if (!readOnly) {
            if (index >= 0 && index < length) {
                cells[index] = text;
                rtn = true;
            } else {
                throw new IndexOutOfBoundsException(index);
            }
        }

        return rtn;
    }

    /**
     * Makes this instance immutable.
     * <p>
     * <b>WARNING:</b> This cannot be undone!
     * </p>
     */
    public void setReadOnly() {
        readOnly = true;
    }

    /**
     * Used by {@link TableRowList#add(TableRow) TableRowList.add(row)} when the row's borderWidth
     * attribute is set to '0' (zero).
     */
    void clearAttributes() {
        if (!readOnly) {
            status.clear();

        }
    }

    private class Status {

        private static final int BORDER = 1;
        private static final int CLASSES = 4;
        private static final int ID = 2;
        private int status = 0;

        Status() {
        }

        public void clear() {
            status = 0;
        }

        public boolean hasAttribute() {
            return status > 0;
        }

        public boolean hasBorder() {
            return (status & BORDER) > 0;
        }

        public void setBorder() {
            status |= BORDER;
            unsetClasses();
        }

        public void unsetBorder() {
            if (hasBorder()) {
                status ^= BORDER;
            }
        }

        public boolean hasClasses() {
            return (status & CLASSES) > 0;
        }

        public void setClasses() {
            status |= CLASSES;
            unsetBorder();
        }

        public void unsetClasses() {
            if (hasClasses()) {
                status ^= CLASSES;
            }
        }

        public boolean hasId() {
            return (status & ID) > 0;
        }

        public void setId() {
            status |= ID;
        }

        public void unsetId() {
            if (hasId()) {
                status ^= ID;
            }
        }
    }
}
