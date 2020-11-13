# GUIMarketplaceDirectory

#### An easy to use GUI based marketplace directory

Do you ever play in a large co-op SMP and spend hours searching for the perfect deals on an item you need in the marketplace? Do you have to spend ages finding what you actually need? GUIMD provides a 
simple easy to use solution to find what you want without the need to traverse around the marketplace.

### How to use
#### Viewing the Marketplace
The shop data is stored in json format in the plugin's directory, making it exportable to other servers (and conversely importable). To make yourself a copy of the Marketplace Directory, sign a book 
with the title `[Marketplace]`. This signed book will now serve as a gateway to the marketplace. Interact with the book as you would do with a regular signed book, and you will be able to see all
shops registered in the directory. You can view the items sold in each shop by clicking on the respective shop names in the inventory 
style GUI now open on your screen.

The items will have their order quantity and value displayed with them. Right click on any item to find better alternatives if they exist.

#### Making your own shop
##### Getting your shop listed
Write the name and description of your shop in a book in the format
````
[<shop-name>]
[<description>]
````
Make sure to not use any more "\[" and "\]" than required. <br>
Sign it with the title `[shop init]` or `[init shop]`. 

##### Adding Items
Open your inventory and right click on an item you want to add with the book as shown:

\<do not forget to add gif link\>

You will be prompted to first enter the unit amount in the form `<shulker>:<stack>:<unit>`, and the the price in diamonds.

Make sure to set quantity as per the rule stated, or you item addition will be aborted. Also, use common sense to add quantities, for eg, if you want to sell a shulker of coal, you could either 
sell coal with the unit amount in the format `1:0:0`, or you could sell a shulker renamed to "Shulker of Coal" with unit amount in the format `0:0:1`.

#### Searching for items
You can search the directory by either items sold, shop name, or owner's name. The `/guimd search` command has been provided. Usage:

`/guimd search item <search-key>`: Returns all items that contain the search key, be it regular items or renamed ones.

`/guimd search shop <search-key>`: Returns all shops that match the search key.

`/guimd search player <search-key>`: Returns all shops whose owner's ign contains the search key.

### Optional Utilities
#### Multi owner support
If enabled, it will allow multiple players to be owners of a shop (as is the case in large SMP's), and allow each of them to add items to the shop. As a side-effect, this will allow players to 
create shops for other players and assign the other player as the primary owner.

The current owner(s) will have to interact with their signed book as they would with a regular signed book, and they will be prompted to add the name of the owner they want to add.

#### Marketplace Directory Moderation
Allowing any player to create shops is a messy ordeal, and there can be rogue entries. If enabled, all new shops will enter a pending perms queue, waiting for the server appointed moderators to 
approve their shop listing. These shop owners can still add items to their shop. Such "pending" shopps won't be displayed in the regular Marketplace Directory. Appointed moderators will have to be 
given the `GUIMD.moderate` permission node. This will enable access to the `/guimd moderate` command. Usage:

`/guimd moderate pending`: This will open up an inventory style GUI similar to the Marketplace Directory, but will only contain shops that need approval. Moderators can right click on a shop to 
approve, or left click to reject. Approval is immediate, but rejection will provide an additional prompt to confirm the action.

`/guimd moderate review`: This will open up an inventory style GUI similar to the Marketplace Directory. Moderators can right click on a shop to remove a shop if they deem it so. This will prompt
the moderator to confirm the removal of the shop.

##### By default, these two utilities are enabled. It is recommended not to turn off moderation.

Additional unused utility: Allow offline players to be added as owners. This has not been added due to the inconclusivity in the decision if it should be added or not. 
