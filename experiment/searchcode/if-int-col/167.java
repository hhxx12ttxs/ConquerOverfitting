/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai;

import java.util.ArrayList;
import model.Dictionary;
import model.GameField;

/**
 * Класс алгоритма поиска подходящего слова, которое можно вписать на поле
 */
public class WordAi {
    
    private GameField _gameField = new GameField();
    private Dictionary _dictionary = new Dictionary();
    private ArrayList<String> _usedWords;
    private ArrayList<ArrayList<String>> temp;
    private int[][] pr;
    private int ai = 0;
    private int aj = 0;
    private int p, t;
    private int curr = 0;
    private String word;
    private boolean tl;
    private boolean bl;
    
    
    /**
     * Конструктор
     * 
     * @param field игровое поле
     * @param usedWords список использованных в игре слов
     */
    public WordAi(GameField field, ArrayList<String> usedWords) {
        _gameField = field;
        _usedWords = usedWords;
    }
    
    /**
     * Ищет все подходящие слова в словаре
     * 
     * @return список подходящих слов
     */
    public ArrayList<String> findWords() {
        
        ArrayList<String> res = new ArrayList<>();
        
        temp = new ArrayList<>();
        for(int row = 0; row < _gameField.width(); row++) {
            temp.add(new ArrayList<String>());
            for(int col = 0; col < _gameField.height(); col++) {
                temp.get(row).add("");
            }
        }
        
        pr = new int[_gameField.height()][_gameField.width()];
        for(int row = 0; row < _gameField.width(); row++) {
            for(int col = 0; col < _gameField.height(); col++) {
                pr[row][col] = 0;
            }
        }

        while (curr < _dictionary.getCount())//Цикл по словарю
        {
            word = _dictionary.getDictionary().get(curr);
            if(!_usedWords.contains(word)) {
                
                for(int i = 0; i < _gameField.width(); ++i) {
                    for(int j = 0; j < _gameField.height(); ++j) {
                        pr[i][j] = 0;
                    }
                }

                tl = true;
                for (int k = 0; k < word.length(); k++) //Цикл по слову из словаря
                {
                    for (int i = 0; i < _gameField.width(); i++) // Цикл по самому массиву.
                    {
                        for (int j = 0; j < _gameField.height(); j++) {
                            if (_gameField.getCellValue(i, j).equals("")) {

                                for (ai = 0; ai < _gameField.width(); ai++) {
                                    for (aj = 0; aj < _gameField.height(); aj++) {
                                        // сохранили массив, для возврата в предыдущее состояние.
                                        temp.get(ai).set(aj, _gameField.getCellValue(ai, aj));
                                    }
                                }

                                _gameField.setCellValue(word.substring(k, k+1), i, j);

                                bl = true;
                                p = k; // С какой буквы подставляли для поиска в конец слова
                                t = k; // С какой буквы подставляли для поиска в начало слова
                                ai = i;
                                aj = j;

                                for(int i1 = 0; i1 < _gameField.width(); ++i1) {
                                    for(int j1 = 0; j1 < _gameField.height(); ++j1) {
                                        pr[i1][j1] = 0;
                                    }
                                }

                                pr[i][j] = 1;
                                for (p = p + 1; p < word.length(); p++) {
                                    if(checkLetter(p)) {
                                        continue;
                                    } else {
                                        break;
                                    }
                                }
                                ai = i;
                                aj = j;
                                if (bl) {
                                    for (t = t - 1; t >= 0; t--) {
                                        if(checkLetter(t)) {
                                            continue;
                                        } else {
                                            break;
                                        }
                                    }

                                    if (bl) {
                                        res.add(word);
                                        for (ai = 0; ai < 5; ai++) {
                                            for (aj = 0; aj < 5; aj++) {
                                                _gameField.setCellValue(temp.get(ai).get(aj), ai, aj);  // восстановили массив, в текущем состоянии.
                                            }
                                        }
                                        tl = false;
                                        break;   // Слово подошло переходим к следующему слову*/
                                    }
                                }

                                for (ai = 0; ai < _gameField.width(); ai++) {
                                    for (aj = 0; aj < _gameField.height(); aj++) {
                                        _gameField.setCellValue(temp.get(ai).get(aj), ai, aj);  // восстановили массив, в текущем состоянии.
                                    }
                                }
                            }
                        }
                        if (!tl) {
                            break;
                        }
                    }
                }
                curr++;
            } else {
                curr++;
            }
        }
        
        return res;
    }
    
    /**
     * Возворащает первое подходящие слово из словаря
     * 
     * @return найденное слово
     */
    public String findWord() {
        
        temp = new ArrayList<>();
        for(int row = 0; row < _gameField.width(); row++) {
            temp.add(new ArrayList<String>());
            for(int col = 0; col < _gameField.height(); col++) {
                temp.get(row).add("");
            }
        }
        
        pr = new int[_gameField.height()][_gameField.width()];
        for(int row = 0; row < _gameField.width(); row++) {
            for(int col = 0; col < _gameField.height(); col++) {
                pr[row][col] = 0;
            }
        }

        while (curr < _dictionary.getCount())//Цикл по словарю
        {
            word = _dictionary.getDictionary().get(curr);
            if(!_usedWords.contains(word)) {
                
                for(int i = 0; i < _gameField.width(); ++i) {
                    for(int j = 0; j < _gameField.height(); ++j) {
                        pr[i][j] = 0;
                    }
                }

                tl = true;
                for (int k = 0; k < word.length(); k++) //Цикл по слову из словаря
                {
                    for (int i = 0; i < _gameField.width(); i++) // Цикл по самому массиву.
                    {
                        for (int j = 0; j < _gameField.height(); j++) {
                            if (_gameField.getCellValue(i, j).equals("")) {

                                for (ai = 0; ai < _gameField.width(); ai++) {
                                    for (aj = 0; aj < _gameField.height(); aj++) {
                                        // сохранили массив, для возврата в предыдущее состояние.
                                        temp.get(ai).set(aj, _gameField.getCellValue(ai, aj));
                                    }
                                }

                                _gameField.setCellValue(word.substring(k, k+1), i, j);

                                bl = true;
                                p = k; // С какой буквы подставляли для поиска в конец слова
                                t = k; // С какой буквы подставляли для поиска в начало слова
                                ai = i;
                                aj = j;

                                for(int i1 = 0; i1 < _gameField.width(); ++i1) {
                                    for(int j1 = 0; j1 < _gameField.height(); ++j1) {
                                        pr[i1][j1] = 0;
                                    }
                                }

                                pr[i][j] = 1;
                                for (p = p + 1; p < word.length(); p++) {
                                    if(checkLetter(p)) {
                                        continue;
                                    } else {
                                        break;
                                    }
                                }
                                ai = i;
                                aj = j;
                                if (bl) {
                                    for (t = t - 1; t >= 0; t--) {
                                        if(checkLetter(t)) {
                                            continue;
                                        } else {
                                            break;
                                        }
                                    }

                                    if (bl) {
                                        return word;
                                    }
                                }

                                for (ai = 0; ai < _gameField.width(); ai++) {
                                    for (aj = 0; aj < _gameField.height(); aj++) {
                                        _gameField.setCellValue(temp.get(ai).get(aj), ai, aj);  // восстановили массив, в текущем состоянии.
                                    }
                                }
                            }
                        }
                        if (!tl) {
                            break;
                        }
                    }
                }
                curr++;
            } else {
                curr++;
            }
        }

        return "";
    }
    
    /**
     * Определяет, есть ли буква на поле сверху или снизу или влева или справа текущей ячейки
     * @param index индекс буквы в слове
     * @return 
     */
    private boolean checkLetter(int index) {
        String letter = word.substring(index, index + 1);
        
        if (ai + 1 != _gameField.width()) {
            if ((_gameField.getCellValue(ai + 1, aj).equals(letter)) 
                    && (pr[ai + 1][aj] != 1)) {
                pr[ai + 1][aj] = 1;
                ai++;
                return true;
            }
        }
        if (ai - 1 != -1) {
            if ((_gameField.getCellValue(ai - 1, aj).equals(letter))
                    && (pr[ai - 1][aj] != 1)) {
                pr[ai - 1][aj] = 1;
                ai--;
                return true;
            }
        }
        if (aj + 1 != _gameField.height()) {
            if ((_gameField.getCellValue(ai, aj + 1).equals(letter))
                    && (pr[ai][aj + 1] != 1)) {
                pr[ai][aj + 1] = 1;
                aj++;
                return true;
            }
        }
        if (aj - 1 != -1) {
            if ((_gameField.getCellValue(ai, aj - 1).equals(letter))
                    && (pr[ai][aj - 1] != 1)) {
                pr[ai][aj - 1] = 1;
                aj--;
                return true;
            }
        }

        bl = false; // Если не совпало ни одно условие выше то это слово не подходит.
        return false;
    }
    
    public String getHelp() {
        String s = findWord();

        for (ai = 0; ai < _gameField.width(); ai++) {
            for (aj = 0; aj < _gameField.height(); aj++) {
                _gameField.setCellValue(temp.get(ai).get(aj), ai, aj);  // восстановили массив, в текущем состоянии.
            }
        }
        return s;
    }
    
}

