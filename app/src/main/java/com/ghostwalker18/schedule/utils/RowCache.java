/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ghostwalker18.schedule.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.util.Iterator;

/**
 * Этот класс служит для реализации буферизированного псевдорандомного доступа
 * к строкам эксель-файла при использовании потокового чтения
 * файла без полной его загрузки в память.
 * <b>Важное ограничение: нельзя возвращаться назад.
 *
 * @author Ипатов Никита
 * @since 2.0
 */
public class RowCache {
    private final Iterator<Row> iterator;
    private int lowBoundary = 0;
    private final int size;
    private Row[] rows;
    private final Row[] oldRows;

    /**
     * Этот метод используется для получения построителя кэша.
     * @return строитель
     */
    public static Builder builder(){
        return new Builder();
    }

    private RowCache(Sheet sheet, int size){
        this.size = size;
        rows = new Row[size];
        oldRows = new Row[size];
        iterator = sheet.rowIterator();
        load();
    }

    /**
     * Этот метод служит для получения строки листа
     * @param row номер строки
     * @return строка
     * @throws IndexOutOfBoundsException если кэш для ряда уже недоступен
     */
    public Row getRow(int row) throws IndexOutOfBoundsException {
        if(row <= lowBoundary - size || row < 0)
            throw new IndexOutOfBoundsException();
        if(row < lowBoundary + size){
            if(row > lowBoundary - size && row < lowBoundary)
                return oldRows[size - (lowBoundary - row)];
            else
                return rows[row - lowBoundary];
        }
        else{
            lowBoundary += size;
            load();
            return getRow(row);
        }
    }

    /**
     * Этот метод загружает новые данные в кэш.
     */
    private void load(){
        if (size >= 0)
            System.arraycopy(rows, 0, oldRows, 0, size);
        rows = new Row[size];
        for(int i = 0; i < size; i++) {
            if(iterator.hasNext())
                rows[i] = iterator.next();
        }
    }

    /**
     * Этот класс служит для построения объекта кэша.
     *
     * @author Ипатов Никита
     */
    public static class Builder{
        private int size = 10;
        private Sheet sheet;

        /**
         * Этот метод задает размер кэша.
         * @param size размер кэша в строках
         * @return объект строителя
         */
        public Builder setSize(int size){
            this.size = size;
            return this;
        }

        /**
         * Этот метод задает лист для кэша.
         * @param sheet лист
         * @return объект строителя
         */
        public Builder setSheet(Sheet sheet){
            this.sheet = sheet;
            return this;
        }

        /**
         * Этот метод строит объект кэша.
         * @return кэш
         */
        public RowCache build(){
            return new RowCache(sheet, size);
        }
    }
}