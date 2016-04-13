//
//  ViewController.swift
//  iShoppy
//
//  Created by Sasha Goldshtein on 4/13/16.
//  Copyright © 2016 Sasha Goldshtein. All rights reserved.
//

import UIKit

class ViewController: UIViewController, UITableViewDataSource, UITableViewDelegate {
    
    @IBOutlet weak var table: UITableView!
    private var serviceClient: MSClient!
    private var serviceTable: MSTable!
    private var items: [NSDictionary]?

    override func viewDidLoad() {
        super.viewDidLoad()
        
        serviceClient = MSClient(applicationURLString: "https://shoppyservice.azure-mobile.net/")
        serviceTable = serviceClient.tableWithName("CartItem")
        serviceClient?.loginWithProvider("twitter", controller: self, animated: true, completion: { (user, error) in
            if let user = user {
                print("Logged in with user: \(user.userId)")
                self.fetchItems()
            } else {
                print("Error logging in: \(error)")
            }
        })
    }
    
    func numberOfSectionsInTableView(tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items?.count ?? 0
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        var cell = tableView.dequeueReusableCellWithIdentifier("ItemCell")
        if cell == nil {
            cell = UITableViewCell(style: .Subtitle, reuseIdentifier: "ItemCell")
        }
        let item: NSDictionary = items![indexPath.row]
        cell?.textLabel?.text = item["title"] as? String
        cell?.detailTextLabel?.text = "buy \(item["amount"]!) of that"
        return cell!
    }
    
    private func fetchItems() {
        serviceTable.readWithCompletion { (result, error) in
            if let error = error {
                print("Error reading from table: \(error)")
            } else {
                self.items = result.items as? [NSDictionary]
                self.table.reloadData()
            }
        }
    }
    
    func tableView(tableView: UITableView, editingStyleForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCellEditingStyle {
        return UITableViewCellEditingStyle.Delete
    }
    
    func tableView(tableView: UITableView, commitEditingStyle editingStyle: UITableViewCellEditingStyle, forRowAtIndexPath indexPath: NSIndexPath) {
        serviceTable.deleteWithId(items![indexPath.row]["id"]) { (_, error) in
            if let error = error {
                print("Error deleting item: \(error)")
            } else {
                self.fetchItems()        // Avoid a full refresh in a real app
            }
        }
    }
    
}

