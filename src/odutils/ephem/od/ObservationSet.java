/* 

Copyright 2021 aholinch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package odutils.ephem.od;

import java.util.List;

import odutils.ephem.CartesianState;

/**
 * This class should be expanded to support weights and other observation types.
 * 
 * @author aholinch
 *
 */
public class ObservationSet 
{
	protected String cartsFrame;
    protected List<CartesianState> carts;
    
    public ObservationSet()
    {
    	
    }
    
    public void setCartsFrame(String frame)
    {
    	cartsFrame = frame;
    }
    
    public String getCartsFrame()
    {
    	return cartsFrame;
    }
    
    public void setCarts(List<CartesianState> list)
    {
    	carts = list;
    }
    
    public List<CartesianState> getCarts()
    {
    	return carts;
    }
}
