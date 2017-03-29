/*
 * Copyright (C) 2017 Bruce Asu<bruceasu@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *  　　
 * 　　The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package asu.patch.builder.util;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by suk on 2017/3/29.
 */
public class Strings {
  /**
   * Concatenates strings, using a separator.
   *
   * @param separator to join with
   * @param strings   to join
   * @return the joined string
   */
  public static String join(CharSequence separator, String... strings) {
    return join(separator, Arrays.asList(strings));
  }

  public static String join(CharSequence separator, Collection<String> collection) {
    // Ideally we don't have to duplicate the code here if array is iterable.
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String s : collection) {
      if (first) {
        first = false;
      } else {
        sb.append(separator);
      }
      sb.append(s);
    }
    return sb.toString();
  }
}
