import requests
import json
import logging

logging.basicConfig(filename='logs/datacrawler.log', filemode='w', format='%(asctime)s - %(message)s', level=logging.DEBUG)

def get_instruments():
 r = requests.get('http://127.0.0.1:48557/instruments')
 logging.debug("Got instruments to watch: " + r.text)
 return r.json()

def request_save(instrument):
 url = 'http://127.0.0.1:48558/bitfinex/spot/' + instrument
 logging.debug("Requesting to save: " + url)
 r = requests.post(url)
 logging.debug("Success when saving? : " + r.text)

def crawling():
 instruments = get_instruments()
 for i in instruments:
  request_save(i["ticker"])

crawling()