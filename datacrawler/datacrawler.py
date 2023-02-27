import requests
import json

def get_instruments():
 r = requests.get('http://127.0.0.1:48557/instruments')
 return r.json()

def request_save(instrument):
 url = 'http://127.0.0.1:48558/bitfinex/spot/' + instrument
 print(url)
 r = requests.post(url)
 print(r.text)

def crawling():
 instruments = get_instruments()
 for i in instruments:
  request_save(i["ticker"])

crawling()