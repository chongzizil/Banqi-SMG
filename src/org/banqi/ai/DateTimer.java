// Copied from: http://bit.ly/1rQubVA

//Copyright 2012 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
////////////////////////////////////////////////////////////////////////////////

package org.banqi.ai;

import java.util.Date;

/**
* Negative milliseconds means there will never be a timeout.
* 
* @author yzibin@google.com (Yoav Zibin)
*/
public class DateTimer implements Timer {
  private long start;
  private int milliseconds;
  
  public DateTimer(int milliseconds) {
    this.milliseconds = milliseconds;
    if (milliseconds > 0) {
      start = now();
    }
  }
  
  public long now() {
    return new Date().getTime();
  }
  
  @Override
  public boolean didTimeout() {
    return milliseconds <= 0 ? false : now() > start + milliseconds;
  }
}