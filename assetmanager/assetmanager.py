import requests
import json

bitfinex = 'http://127.0.0.1:48558/'
ordenanza = 'http://127.0.0.1:48557/'
threshold = 0.01 # Deviation of 1% at least

def get_weights():
 url = ordenanza + "basket/all/"
 r = requests.get(url)
 data = r.json()
 weights = data[0]["weights"]
 info = {}
 for w in weights:
  ccy = w["currency"]
  info[ccy] = {"weight": w["weight"]}
 return info

def get_positions(info):
 url = bitfinex + "position/last/"
 r = requests.get(url)
 data = r.json()
 for d in data:
  ### We are considering currencies with a weight defined only
  if (d["currency"] in info):
   ### We are discarding positions below 100€ / 20000[EURBTC] = 0.005
   if (float(d["quantity"]) > 0.005):
    q = d["quantity"]
   else:
    q = 0
   info[d["currency"]]["position_ccy"] = q

def get_spots(info):
 url = bitfinex + "bitfinex/spots/"
 for c in info.keys():
  url = url + "," + c
 r = requests.get(url)
 data = r.json()
 for d in data:
  ccy = d["instrument"].replace("BTC", "")
  info[ccy]["spot"] = {}
  info[ccy]["spot"]["bid"] = d["bid"]
  info[ccy]["spot"]["offer"] = d["offer"]

def get_btc_equivalent(info):
 eq = 0
 for ccy in info.keys():
  if ccy == "BTC":
   this_eq = info[ccy]["position_ccy"]
  else:
   this_eq = info[ccy]["position_ccy"] * info[ccy]["spot"]["bid"]
  info[ccy]["position_btc"] = this_eq
  eq = eq + this_eq
 return eq

def get_bounds(btc, info):
 bounds = {}
 for ccy in info.keys():
  if "BTC" != ccy:
   bounds[ccy] = {}
   w = info[ccy]["weight"]
   s = info[ccy]["spot"]
   bounds[ccy]["upper"] = w * btc / s["bid"]
   bounds[ccy]["lower"] = w * btc / s["offer"]
 return bounds

def get_selling_trades(trades, btc, bounds, info, threshold):
 for i in info.keys():
  print(i)
  if "BTC" != i:
   local_bound = bounds[i]["upper"]
   diff = info[i]["position_ccy"] - local_bound
   print("diff")
   print(diff)
   local_spot = info[i]["spot"]["bid"]
   local_threshold = threshold * btc * local_spot
   print("local_threshold")
   print(local_threshold)
   if diff > local_threshold :
    trade = {"pair": i + "BTC", "amount": -diff}
    trades.append(trade)

def get_buying_trades(trades, btc, bounds, info, threshold):
 for i in info.keys():
  print(i)
  if "BTC" != i:
   local_bound = bounds[i]["lower"]
   diff = local_bound - info[i]["position_ccy"]
   print("diff")
   print(diff)
   local_spot = info[i]["spot"]["offer"]
   local_threshold = threshold * btc * local_spot
   print("local_threshold")
   print(local_threshold)
   if diff > local_threshold :
    trade = {"pair": i + "BTC", "amount": diff}
    trades.append(trade)

info = get_weights()
get_positions(info)
get_spots(info)
btc_eq = get_btc_equivalent(info)
print("btc total")
print(btc_eq)
bounds = get_bounds(btc_eq, info)
print("bounds: ")
print(bounds)
trades = []
print("info: ")
print(info)
get_selling_trades(trades, btc_eq, bounds, info, threshold)
get_buying_trades(trades, btc_eq, bounds, info, threshold)
print("trades: ")
print(trades)
# call trader