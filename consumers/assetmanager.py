import requests
import json
import datetime
import logging

logging.basicConfig(filename='logs/assetmanager.log', filemode='w', format='%(asctime)s - %(message)s', level=logging.DEBUG)
bitfinex = 'http://127.0.0.1:48558/'
ordenanza = 'http://127.0.0.1:48557/'
threshold = 0.01 # Deviation of 1% at least

# Doc at the end of the file

def get_position(info, ccy):
 if ccy in info and "position_ccy" in info[ccy]:
  p = info[ccy]["position_ccy"]
 else:
  logging.warning("Position default set to ZERO because currency does not have any value: " + ccy)
  p = 0
 return p

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
 logging.info("Got positions: " + r.text)
 for d in data:
  ### We are considering currencies with a weight defined only
  if (d["currency"] in info):
   ### We are discarding positions below 10€ / 20000[EURBTC] = 0.0005
   if (float(d["quantity"]) > 0.0005):
    q = d["quantity"]
   else:
    logging.warning("Position < 0.0005, considering it as zero [" + d["currency"] + "]")
    q = 0
   info[d["currency"]]["position_ccy"] = q

def get_spots(info):
 url = bitfinex + "bitfinex/spots/"
 for c in info.keys():
  url = url + "," + c
 r = requests.get(url)
 data = r.json()
 logging.info("Got spots: " + r.text)
 for d in data:
  ccy = d["instrument"].replace("BTC", "")
  info[ccy]["spot"] = {}
  info[ccy]["spot"]["bid"] = d["bid"]
  info[ccy]["spot"]["offer"] = d["offer"]

def get_btc_equivalent(info):
 eq = 0
 for ccy in info.keys():
  if ccy == "BTC":
   this_eq = get_position(info, ccy)
  else:
   this_eq = get_position(info, ccy) * info[ccy]["spot"]["bid"]
  info[ccy]["position_btc"] = this_eq
  eq = eq + this_eq
 logging.info("Current positions are " + str(eq) + "BTC worth")
 logging.debug("info: " + str(info))
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
 logging.debug("Bounds for currencies: " + str(bounds))
 return bounds

def get_selling_trades(trades, btc, bounds, info, threshold):
 t = datetime.datetime.now()
 timestamp = t.strftime("%Y-%m-%d %H:%M:%S")
 for i in info.keys():
  if "BTC" != i:
   local_bound = bounds[i]["upper"]
   diff = get_position(info, i) - local_bound
   local_spot = info[i]["spot"]["bid"]
   local_threshold = threshold * btc * local_spot
   if diff > local_threshold :
    trade = {"pair": i + "BTC", "amount": -diff}
    trades.append(trade)

def get_buying_trades(trades, btc, bounds, info, threshold):
 t = datetime.datetime.now()
 timestamp = t.strftime("%Y-%m-%d %H:%M:%S")
 for i in info.keys():
  if "BTC" != i:
   local_bound = bounds[i]["lower"]
   diff = local_bound - get_position(info, i)
   local_spot = info[i]["spot"]["offer"]
   local_threshold = threshold * btc * local_spot
   if diff > local_threshold :
    trade = {"pair": i + "BTC", "amount": diff, "issuedTimestamp": timestamp}
    trades.append(trade)

def trade(trades):
 logging.info("Sending trades: " + str(trades))
 if len(trades) > 0:
  url = bitfinex + "bitfinex/trade/"
  requests.post(url, json=trades)

info = get_weights()
get_positions(info)
get_spots(info)
logging.debug(info)
btc_eq = get_btc_equivalent(info)
bounds = get_bounds(btc_eq, info)
trades = []
logging.info(info)
get_selling_trades(trades, btc_eq, bounds, info, threshold)
get_buying_trades(trades, btc_eq, bounds, info, threshold)
trade(trades)

# DOC #

#info[<ccy>]: {
#   position_ccy: current position in ccy units,
#   position_btc: current position in BTC,
#   spot: {
#       bid: float,
#       offer:
#   }
#   weight: desired weight in x per 1
#}
#
#bounds[ccy]: {
#   upper: weight * btc_eq_tot / spot["bid"],
#   lower: weight * btc_eq_tot / spot["offer"]
#}