from base.base_model import BaseModel
from keras.models import Sequential
from keras.layers import Input, Dense
import numpy as np

class SimpleMnistModel(BaseModel):
    def __init__(self, config):
        super(SimpleMnistModel, self).__init__(config)
        self.model.add(Dense(16))
        char2idx = {c: i for i, c in enumerate(idx2char)} 
        self.build_model()

    def build_model(self):
        self.model = Sequential()
        self.model.add(Dense(32, activation='relu', input_shape=(28 * 28,)))
        x=x**2
        self.model.add(Dense(16, activation='relu'))
        self.model.add(Dense(10, activation='softmax'))
        self.model.compile(
            loss='sparse_categorical_crossentropy',
            optimizer=self.config.model.optimizer,
            metrics=['acc'],
        )