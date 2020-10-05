from nearestNeighbors.CollaborativeFilteringCore import elaborate_features
from nearestNeighbors.LoadModel import load_model
from nearestNeighbors.StreamingLearning import Streaming_learning
import numpy as np
import pandas as pd
import logging


# Controller class
class RecommendationController:
    streaming_learning = 0
    restaurant_features = 0
    n_neighbors = 0
    # FEATURES_PATH it's an internal resources
    FEATURES_PATH = './model/restourant_features.sav'

    def __init__(self, n_neighbors):
        super()
        self.n_neighbors = n_neighbors
        self.restaurant_features = load_model(self.FEATURES_PATH)
        self.streaming_learning = Streaming_learning(n_neighbors)

    # Utility to get index by name of restaurant list
    def get_right_index(self, name):
        I = 0
        index = 0
        for x in self.restaurant_features.index:
            if x == '"'+name+'"':
                index = I
                break
            else:
                I = I + 1
        return index

    # Find nearest interest for a certain user
    def get_recommendation(self, name):
        index = self.get_right_index(name)
        distances, indices = self.streaming_learning.get_nearest(self.restaurant_features.iloc[index]
                                                                 .values.reshape(1, -1))
        array = []
        for i in range(0, len(distances.flatten())):
            if i != 0:
                array.append(self.restaurant_features.index[indices.flatten()[i]])
        return array

    # Keep active model learning
    def learn_from_stream(self, user_id, restourant_name, rating):
        data = pd.DataFrame({'name': self.get_right_index(restourant_name), 'user_id': user_id, 'restaurant_rating': rating},
                            index=[0])
        self.streaming_learning.fit_one(data)
        print('model updated')
